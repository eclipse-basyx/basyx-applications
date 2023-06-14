using BaSyx.AAS.Server.Http;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Export;
using BaSyx.Utils.Settings.Types;

using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using TestDemonstrator.Enums;
using TestDemonstrator.TestObjectProviders;
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;

namespace TestDemonstrator.Tests.TestObjectProviderTests
{
    public class TestObjectProviderTests
    {
        private ITestObjectProvider? provider = null;
        private ITicketRepository? repository = null;
        private ITicketBuilder? builder = null;
        private IDescriptorFactory? factory = null;

        private ITestTicket? passiveTicket = null;
        private ITestTicket? activeTicket = null;
        private List<ITestTicket>? uploadedTickets = null;
        private AssetAdministrationShellHttpServer? aasServer = null;

        private static string root = @"../../../TestObjectProviderTests/TestData";

        [OneTimeSetUp]
        public void OneTimeSetup()
        {
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                .AddJsonFile("appsettings.Development.json")
                .Build();

            DbContextOptions<TestTicketContext> options = new DbContextOptionsBuilder<TestTicketContext>()
                .UseSqlServer(configuration.GetConnectionString("SqlServerConnection"))
                .Options;

            factory = new DescriptorFactory();
            builder = new TicketBuilder();
            repository = new TicketDbRepository(options);
            provider = new TestObjectProvider(repository);
        }

        [SetUp]
        public void Setup()
        {
            (repository as TicketDbRepository)!.dbContext!.Database.EnsureDeleted();
            (repository as TicketDbRepository)!.dbContext!.Database.EnsureCreated();

            uploadedTickets = new List<ITestTicket>();

            //Uploading active tickets to the repository
            foreach (var descriptorsRoot in Directory.EnumerateDirectories($"{root}/ActiveTicketDescriptors"))
            {
                //Reading contents
                var descriptors = Directory.EnumerateFiles(descriptorsRoot).ToArray();
                var aasContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("AAS.xml")).Single());
                var structDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SD.xml")).Single());
                var statesDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SMD.xml")).Single());
                var uri = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith(".txt")).Single());

                //Instantiating contents
                var structDescr = factory!.CreateStructureDescriptor(structDescrContent);
                var statesDescr = factory!.CreateStateMachineDescriptor(statesDescrContent);
                var aas = AssetAdministrationShellEnvironment_V2_0
                    .ReadEnvironment_V2_0($"{root}/ActiveTicketDescriptors/{descriptors.Where(x => x.EndsWith("AAS.xml")).Single()}")
                    .AssetAdministrationShells
                    .First();

                //Creating the test object
                ServerSettings aasServerSettings = ServerSettings.CreateSettings();
                aasServerSettings.ServerConfig.Hosting.Urls.Add(uri);
                Identifier identifier = new Identifier("Test", KeyType.Custom);
                IAssetAdministrationShellServiceProvider serviceProvider = aas.CreateServiceProvider(true);
                serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);
                aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
                aasServer.SetServiceProvider(serviceProvider);
                aasServer.RunAsync();

                // Building an active ticket
                activeTicket = builder!
                    .WithType(TicketType.Active)
                    .WithStructureDescriptor(structDescr)
                    .WithStateMachineDescriptor(statesDescr)
                    .WithUrl(new Uri(uri))
                    .Build();

                repository!.Upload(activeTicket);

                uploadedTickets!.Add(activeTicket);
            }

            //Uploading passive tickets to the repository
            foreach (var descriptorsRoot in Directory.EnumerateDirectories($"{root}/PassiveTicketDescriptors"))
            {
                //Reading contents
                var descriptors = Directory.EnumerateFiles(descriptorsRoot).ToArray();
                var aasContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("AAS.xml")).Single());
                var structDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SD.xml")).Single());

                //Instantiating contents
                var structDescr = factory!.CreateStructureDescriptor(structDescrContent);
                var aas = AssetAdministrationShellEnvironment_V2_0
                    .ReadEnvironment_V2_0($"{root}/ActiveTicketDescriptors/{descriptors.Where(x => x.EndsWith("AAS.xml")).Single()}")
                    .AssetAdministrationShells
                    .First();

                // Building a passive ticket
                passiveTicket = builder!
                    .WithType(TicketType.Passive)
                    .WithStructureDescriptor(structDescr)
                    .WithPassiveAdministrationShell(aas)
                    .Build();

                repository!.Upload(passiveTicket);

                uploadedTickets!.Add(passiveTicket);
            }
        }

        //TC.TOP01
        [Test]
        public void RetrieveTestObject_RetrievesValidTestObject() 
        {
            var passiveTestObject = provider!.RetrieveTestObject(passiveTicket!);
            var activeTestObject = provider!.RetrieveTestObject(passiveTicket!);
            
            Assert.NotNull(passiveTestObject);
            Assert.NotNull(activeTestObject);
        }

        //TC.TOP02
        [Test]
        public void RetrieveTestObjects_TestObjectsCountEqualsUploadedTicketsCount()
        {
            Assert.That(provider!.RetrieveTestObjects().Count(), Is.EqualTo(uploadedTickets!.Count));
        }

        //TC.TOP03
        [Test]
        public void RetrievePassiveTestObjects_TestObjectsCountEqualsUploadedPassiveTicketsCount()
        {
            var retrievedPassiveObjectsCount = provider!.RetrievePassiveTestObjects().Count();
            var uploadedPassiveTicketsCount = uploadedTickets!.Where(x => x.Type == TicketType.Passive).Count();
            Assert.That(retrievedPassiveObjectsCount, Is.EqualTo(uploadedPassiveTicketsCount));
        }

        //TC.TOP04
        [Test]
        public void RetrieveActiveTestObjects_TestObjectsCountEqualsUploadedActiveTicketsCount()
        {
            var retrievedActiveObjectsCount = provider!.RetrieveActiveTestObjects().Count();
            var uploadedActiveTicketsCount = uploadedTickets!.Where(x => x.Type == TicketType.Active).Count();
            Assert.That(retrievedActiveObjectsCount, Is.EqualTo(uploadedActiveTicketsCount));
        }

        //TC.TOP05
        [Test]
        public void RetrieveProcessedTestObjects_TestObjectsCountEqualsProcessedTicketsCount()
        {
            var retrievedProcessedObjectsCount = provider!.RetrieveProcessedTestObjects().Count();
            var processedTicketsCount = uploadedTickets!.Where(x => x.TestReport != null).Count();
            Assert.That(retrievedProcessedObjectsCount, Is.EqualTo(processedTicketsCount));
        }

        //TC.TOP06
        [Test]
        public void RetrieveUnprocessedTestObjects_TestObjectsCountEqualsUnprocessedTicketsCount()
        {
            var retrievedUnrocessedObjectsCount = provider!.RetrieveUnprocessedTestObjects().Count();
            var unprocessedTicketsCount = uploadedTickets!.Where(x => x.TestReport == null).Count();
            Assert.That(retrievedUnrocessedObjectsCount, Is.EqualTo(unprocessedTicketsCount));
        }

        //TC.TOP07
        [Test]
        public void RetrieveTestObjectById_RetrievesTestObjectOnUploadedTicketWithKnownId() 
        {
            var id = repository!.Tickets!.First().Id;
            var testObject = provider!.RetrieveTestObjectById(id);
            Assert.That(testObject, Is.Not.Null);
        }

        //TC.TOP08
        [Test]
        public void RetrieveTestObjectById_UnknownTicketId_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => provider!.RetrieveTestObjectById(new Guid()));
        }

        //TC.TOP09
        [Test]
        public void RetrieveTestObjectByUri_RetrievesTestObjectOnUploadedTicketWithKnownUri()
        {
            var uri = repository!.Tickets!.Where(t => t.Uri != null).First().Uri;
            var testObject = provider!.RetrieveTestObjectByUrl(uri!.ToString());
            Assert.That(testObject, Is.Not.Null);
        }

        //TC.TOP10
        [Test]
        public void RetrieveTestObjectByUri_UnknownTicketUri_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => provider!.RetrieveTestObjectByUrl("http://localhost:5000"));
        }
    }
}

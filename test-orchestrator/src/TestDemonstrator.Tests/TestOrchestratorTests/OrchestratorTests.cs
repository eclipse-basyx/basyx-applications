using BaSyx.AAS.Server.Http;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Export;
using BaSyx.Utils.Settings.Types;

using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System.Xml;

using TestDemonstrator.Enums;
using TestDemonstrator.Orchestrator;
using TestDemonstrator.TestObjectProviders;
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestRunners;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.Tests.TestOrchestratorTests.TestSuites;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;

namespace TestDemonstrator.Tests.TestOrchestratorTests
{
    public class OrchestratorTests
    {
        //Core components
        private IDescriptorFactory? factory = null;
        private ITicketBuilder? builder = null;
        private ITicketRepository? repository = null;
        private ITestObjectProvider? provider = null;
        private ITestRunner? runner = null;
        private TestOrchestrator? orchestrator = null;

        //Secondary variables 
        private ICollection<ITestTicket>? tickets = null;
        private AssetAdministrationShellHttpServer? aasServer = null;
        private static string root = @"../../../TestOrchestratorTests/TestData";

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
            runner = new NUnitTestRunner();
            orchestrator = new TestOrchestrator();
        }

        [SetUp]
        public void SetUp()
        {
            tickets = new List<ITestTicket>();
            (repository as TicketDbRepository)!.dbContext!.Database.EnsureDeleted();
            (repository as TicketDbRepository)!.dbContext!.Database.EnsureCreated();

            //Uploading active tickets to the repository
            foreach (var descriptorsRoot in Directory.EnumerateDirectories($"{root}/ActiveTicketDescriptors"))
            {
                //Reading contents
                var descriptors = Directory.EnumerateFiles(descriptorsRoot).ToArray();
                var aasContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("AAS.xml")).Single());
                var structDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SD.xml")).Single());
                var statesDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SMD.xml")).Single());
                var uri = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith(".txt")).Single());

                //Instantiating descriptors
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
                var activeTicket = builder!
                    .WithType(TicketType.Active)
                    .WithStructureDescriptor(structDescr)
                    .WithStateMachineDescriptor(statesDescr)
                    .WithUrl(new Uri(uri))
                    .Build();

                repository!.Upload(activeTicket);
                tickets!.Add(activeTicket);
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
                var passiveTicket = builder!
                    .WithType(TicketType.Passive)
                    .WithStructureDescriptor(structDescr)
                    .WithPassiveAdministrationShell(aas)
                    .Build();

                repository!.Upload(passiveTicket);
                tickets!.Add(passiveTicket);
            }
        }

        //TC.OR01
        [Test]
        public void ExecuteTestPipeline_ExecutesAndProtocolsSingleTestSuite()
        {
            provider!
                .RetrieveTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01) };
                    orchestrator!.ExecuteTestPipeline(ref testObject, repository!, runner!, testClasses);
                    Assert.IsNotNull(testObject.Ticket.TestReport);

                    var doc = new XmlDocument();
                    doc.LoadXml(testObject.Ticket.TestReport);
                    var nodes = doc.SelectNodes("PipelineReport/ExamplePipelineLevel01");

                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR02
        [Test]
        public void ExecuteTestPipeline_ExecutesAndProtocolsInvalidTestSuite_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() =>
                provider!
                    .RetrieveTestObjects()
                    .ToList()
                    .ForEach(testObject =>
                    {
                        var testClasses = new Type[] { typeof(int) };
                        orchestrator!.ExecuteTestPipeline(ref testObject, repository!, runner!, testClasses);
                    }));
        }

        //TC.OR01 (complementary)
        [Test]
        public void ExecuteTestPipeline_ExecutesAndProtocolsMultipleTestSuites()
        {
            provider!
                .RetrieveTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
                    orchestrator!.ExecuteTestPipeline(ref testObject, repository!, runner!, testClasses);
                    Assert.IsNotNull(testObject.Ticket.TestReport);

                    var doc = new XmlDocument();
                    doc.LoadXml(testObject.Ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR03 
        [Test]
        public void ExecuteTestPipelineById_ExecutesAndProtocolsTestSuite()
        {
            repository!
                .Tickets
                .ToList()
                .ForEach(ticket => {
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
                    orchestrator!.ExecuteTestPipelineById(ticket.Id, provider!, runner!, testClasses);

                    var testObject = provider!.RetrieveTestObjectById(ticket.Id);
                    Assert.IsNotNull(testObject!.Ticket.TestReport);

                    var doc = new XmlDocument();
                    doc.LoadXml(testObject.Ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });   
        }

        //TC.OR04 
        [Test]
        public void ExecuteTestPipelineById_OnUnknownTicketId_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => {
                tickets!.ToList().ForEach(ticket =>
                {
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
                    orchestrator!.ExecuteTestPipelineById(new Guid(), provider!, runner!, testClasses);
                });
            });
        }

        //TC.OR05
        [Test]
        public void ExecuteTestPipelineByUri_ExecutesAndProtocolsTestSuite()
        {
            repository!
                .Tickets
                .Where(ticket => ticket.Type == TicketType.Active)
                .ToList()
                .ForEach(ticket => {
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
                    orchestrator!.ExecuteTestPipelineByUri(ticket.Uri!, provider!, runner!, testClasses);

                    var testObject = provider!.RetrieveTestObjectByUrl(ticket.Uri!.ToString());
                    Assert.IsNotNull(testObject!.Ticket.TestReport);

                    var doc = new XmlDocument();
                    doc.LoadXml(testObject.Ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR06
        [Test]
        public void ExecuteTestPipelineByUri_OnUnknownUri_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => {
                    var unregisteredUri = new Uri("http://localhost:5000");
                    var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
                    orchestrator!.ExecuteTestPipelineByUri(unregisteredUri, provider!, runner!, testClasses);
            });
        }

        //TC.OR07
        [Test]
        public void ExecuteTestPipelines_ExecutesAndProtocolsTestSuitesForEachRegisteredTicket()
        {
            var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
            orchestrator!.ExecuteTestPipelines(provider!, runner!, testClasses);

            repository!
                .Tickets
                .ToList()
                .ForEach(ticket =>
                {
                    var doc = new XmlDocument();
                    doc.LoadXml(ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(ticket.TestReport);
                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR08
        [Test]
        public void ExecutePassiveTestPipelines_ExecutesAndProtocolsTestSuitesForEachRegisteredTicket()
        {
            var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
            orchestrator!.ExecutePassiveTestPipelines(provider!, runner!, testClasses);

            Assert.That(repository!
                            .Tickets
                            .Where(ticket => ticket.Type == TicketType.Passive)
                            .Count(), Is.EqualTo(repository!
                                                    .Tickets
                                                    .Where(ticket => ticket.TestReport != null)
                                                    .Count()));
            repository!
                .Tickets
                .Where(ticket => ticket.Type == TicketType.Passive)
                .ToList()
                .ForEach(ticket =>
                {
                    var doc = new XmlDocument();
                    doc.LoadXml(ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(ticket.TestReport);
                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR09
        [Test]
        public void ExecuteActiveTestPipelines_ExecutesAndProtocolsTestSuitesForEachRegisteredTicket()
        {
            var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
            orchestrator!.ExecuteActiveTestPipelines(provider!, runner!, testClasses);


            Assert.That(repository!
                            .Tickets
                            .Where(ticket => ticket.Type == TicketType.Active)
                            .Count(), Is.EqualTo(repository!
                                                    .Tickets
                                                    .Where(ticket => ticket.TestReport != null)
                                                    .Count()));

            repository!
                .Tickets
                .Where(ticket => ticket.Type == TicketType.Active)
                .ToList()
                .ForEach(ticket =>
                {
                    var doc = new XmlDocument();
                    doc.LoadXml(ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(ticket.TestReport);
                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR10
        [Test]
        public void ExecuteProcessedTestPipelines_ExecutesAndProtocolsTestSuitesForEachRegisteredTicket()
        {
            var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
            orchestrator!.ExecuteProcessedTestPipelines(provider!, runner!, testClasses);

            Assert.That(repository!
                            .Tickets
                            .Where(ticket => ticket.TestReport == null)
                            .Count(), Is.EqualTo(tickets!.Count));

            repository!
                .Tickets
                .Where(ticket => ticket.TestReport != null)
                .ToList()
                .ForEach(ticket =>
                {
                    var doc = new XmlDocument();
                    doc.LoadXml(ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(ticket.TestReport);
                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }

        //TC.OR11
        [Test]
        public void ExecuteUnprocessedTestPipelines_ExecutesAndProtocolsTestSuitesForEachRegisteredTicket()
        {
            var testClasses = new Type[] { typeof(ExamplePipelineLevel01), typeof(ExamplePipelineLevel02) };
            orchestrator!.ExecuteUnprocessedTestPipelines(provider!, runner!, testClasses);

            Assert.That(repository!
                .Tickets
                .Where(ticket => ticket.TestReport != null)
                .Count(), Is.EqualTo(tickets!.Count()));

            repository!
                .Tickets
                .Where(ticket => ticket.TestReport != null)
                .ToList()
                .ForEach(ticket =>
                {
                    var doc = new XmlDocument();
                    doc.LoadXml(ticket.TestReport);
                    var nodes = doc.SelectSingleNode("PipelineReport")!.ChildNodes;

                    Assert.IsNotNull(ticket.TestReport);
                    Assert.IsNotNull(nodes);
                    Assert.That(nodes.Count, Is.EqualTo(testClasses.Count()));
                });
        }
    }
}

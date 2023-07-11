//External libraries
using BaSyx.AAS.Client.Http;
using BaSyx.AAS.Server.Http;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Export;
using BaSyx.Utils.Settings.Types;

using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

//Core components
using TestDemonstrator.Enums;
using TestDemonstrator.Orchestrator;
using TestDemonstrator.TestObjectProviders;
using TestDemonstrator.TestRunners;
using TestDemonstrator.TestSuites;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Data;

namespace TestDemonstrator
{
    internal class Program
    {
        static void Main(string[] args)
        {
            //Reading the connection string and setting the database connection
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                .AddJsonFile("appsettings.Development.json")
                .Build();

            DbContextOptions<TestTicketContext> options = new DbContextOptionsBuilder<TestTicketContext>()
                .UseSqlServer(configuration.GetConnectionString("SqlServerConnection"))
                .Options;

            //Core components 
            var factory = new DescriptorFactory();
            var builder = new TicketBuilder();
            var repository = new TicketDbRepository(options);
            var provider = new TestObjectProvider(repository);
            var runner = new NUnitTestRunner();
            var orchestrator = new TestOrchestrator();

            //local variables for the purpose of the demo
            AssetAdministrationShellHttpServer aasServer;
            var root = @"../../../TestData";
            ITestTicket tick = null!;

            //Uploading active tickets to the repository
            foreach (var descriptorsRoot in Directory.EnumerateDirectories($"{root}/ActiveTicketDescriptors"))
            {
                //Reading contents
                var descriptors = Directory.EnumerateFiles(descriptorsRoot).ToArray();
                var aasContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("AAS.xml")).Single());
                var structDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("ST.xml")).Single());
                var statesDescrContent = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith("SMD.xml")).Single());
                var uri = System.IO.File.ReadAllText(descriptors.Where(x => x.EndsWith(".txt")).Single());

                //Instantiating descriptors
                var structDescr = factory!.CreateStructureDescriptor(structDescrContent);
                var statesDescr = factory!.CreateStateMachineDescriptor(statesDescrContent);
                var aas = AssetAdministrationShellEnvironment_V2_0
                      .ReadEnvironment_V2_0($"{descriptors.Where(x => x.EndsWith("PackML.xml")).Single()}")
                      .AssetAdministrationShells
                      .First();
#if false
                //Creating and running an AAS from the ticket info
                ServerSettings aasServerSettings = ServerSettings.CreateSettings();
                aasServerSettings.ServerConfig.Hosting.Urls.Add(uri);
                Identifier identifier = new Identifier("Test", KeyType.Custom);
                IAssetAdministrationShellServiceProvider serviceProvider = aas.CreateServiceProvider(true);
                serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);
                aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
                aasServer.SetServiceProvider(serviceProvider);
                aasServer.RunAsync();
#endif
                //Building an active test ticket corresponding to the AAS
                var activeTicket = builder!
                    .WithType(TicketType.Active)
                    .WithStructureDescriptor(structDescr)
                    .WithStateMachineDescriptor(statesDescr!)
                    .WithUrl(new Uri(uri))
                    .Build();

                tick = activeTicket;

                //Uploading the ticket to the ticket repository
                repository!.Upload(activeTicket);
            }
#if false
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
                    .ReadEnvironment_V2_0($"{descriptors.Where(x => x.EndsWith("AAS.xml")).Single()}")
                    .AssetAdministrationShells
                    .First();

                // Building a passive ticket
                var passiveTicket = builder!
                    .WithType(TicketType.Passive)
                    .WithStructureDescriptor(structDescr)
                    .WithPassiveAdministrationShell(aas)
                    .Build();

                //Uploading the ticket to the ticket repository
                repository!.Upload(passiveTicket);
            }
#endif
            //Defining a test pipeline
            var pipeline = new Type[] { typeof(StructureTests), typeof(StateMachineTests) };

            //Obtaining the test object of the first ticket
            var subject = provider.RetrieveTestObjectById(tick.Id);

            //Running the pipeline on the test object 
            orchestrator.ExecuteTestPipeline(ref subject, repository, runner, pipeline);

            //Obtaining the test ticket
            var ticket = repository.RetrieveTicketById(subject.Ticket.Id);

            //Printing the test report
            Console.WriteLine(ticket!.TestReport);
        }
    }
}
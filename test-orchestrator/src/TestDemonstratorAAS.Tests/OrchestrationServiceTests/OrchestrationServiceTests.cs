
using BaSyx.AAS.Server.Http;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Export;
using BaSyx.Utils.Settings.Types;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using TestDemonstrator.Orchestrator;
using TestDemonstrator.Orchestrator.Contracts;
using TestDemonstrator.TestObjectProviders;
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestRunners;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.Tests.TestOrchestratorTests.TestSuites;
using TestDemonstrator.TestSuites;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;
using TestDemonstratorAAS.OrchestrationService;
using TestDemonstratorAAS.RepositoryService;

namespace TestDemonstratorAAS.Tests.OrchestrationServiceTests
{
    public class OrchestrationServiceTests
    {
        private IDescriptorFactory descriptorFactory = null!;
        private ITicketBuilder ticketBuilder = null!;
        private ITicketRepository ticketRepository = null!;
        private ITestRunner testRunner = null!;

        private ITestObjectProvider testObjectProvider = null!;
        private ICollection<Type> testPipeline = null!;
        private ITestOrchestrator testOrchestrator = null!;

        private TicketRepositoryService repositoryService = null!;
        private TestOrchestrationService orchestrationService = null!;

        private AssetAdministrationShellHttpServer? aasServer = null;
        private static string root = $"../../../TicketRepositoryServiceTests/TestData";

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

            descriptorFactory = new DescriptorFactory();
            ticketBuilder = new TicketBuilder();
            ticketRepository = new TicketDbRepository(options);

            testRunner = new NUnitTestRunner();
            testObjectProvider = new TestObjectProvider(ticketRepository);
            testPipeline = new Type[] { typeof(ExamplePipelineLevel01) };
            testOrchestrator = new TestOrchestrator();

            repositoryService = new TicketRepositoryService(ticketRepository, ticketBuilder, descriptorFactory);
            orchestrationService = new TestOrchestrationService(testOrchestrator, testObjectProvider, testRunner, testPipeline);
        }

        [SetUp]
        public void Setup()
        {
            (ticketRepository as TicketDbRepository)!.dbContext!.Database.EnsureDeleted();
            (ticketRepository as TicketDbRepository)!.dbContext!.Database.EnsureCreated();
        }

        //TC.TOS01
        [Test]
        [TestCase(" ")]
        [TestCase(null)]
        public void ExecuteTestPipelineById_WithInvalidId_ThrowsException(string id)
        {
            Assert.Throws<ArgumentException>(() => orchestrationService.ExecuteTestPipelineById(id));
        }

        //TC.TOS02
        [Test]
        public void ExecuteTestPipelineById_WithUnknownId_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => orchestrationService.ExecuteTestPipelineById(new Guid().ToString()));
        }

        //TC.TOS03
        private static TestCaseData[] ExecuteTestPipelineById_TicketInfo =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(ExecuteTestPipelineById_TicketInfo))]
        public void ExecuteTestPipelineById_RunsTestsCreatesProtocolAndReturnsSuccessMessage(string passiveShellContent, string structureDescriptor)
        {
            repositoryService.UploadPassiveTicket(passiveShellContent, structureDescriptor);
            var ticket = ticketRepository.Tickets.First();
            var message = orchestrationService.ExecuteTestPipelineById(ticket.Id.ToString());

            Assert.That(message, Is.EqualTo($"Successfully executed test pipeline for ticket with Id: {ticket.Id}"));
            Assert.That(ticketRepository.Tickets.First().TestReport, Is.Not.Null);
        }

        //TC.TOS04
        [Test]
        [TestCase(" ")]
        [TestCase(null)]
        public void ExecuteTestPipelineByUri_WithInvalidUri_ThrowsException(string uri)
        {
            Assert.Throws<ArgumentException>(() => orchestrationService.ExecuteTestPipelineByUri(uri));
        }

        //TC.TOS05
        [TestCase("http://localhost:4999")]
        public void ExecuteTestPipelineByUri_WithUnknownUri_ThrowsException(string uri)
        {
            Assert.Throws<ArgumentException>(() => orchestrationService.ExecuteTestPipelineById(uri));
        }

        //TC.TOS06
        private static TestCaseData[] ExecuteTestPipelineByUri_TicketInfo =
        {
             new TestCaseData(
                 "http://localhost:4999/",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
        };
        [TestCaseSource(nameof(ExecuteTestPipelineByUri_TicketInfo))]
        public void ExecuteTestPipelineByUri_RunsTestsCreatesProtocolAndReturnsSuccessMessage(string uriString, string structureDescriptor, string statesDescriptor, string aasPath)
        {
            var aas = AssetAdministrationShellEnvironment_V2_0
                .ReadEnvironment_V2_0(aasPath)
                .AssetAdministrationShells
                .First();

            //Creating the test object
            ServerSettings aasServerSettings = ServerSettings.CreateSettings();
            aasServerSettings.ServerConfig.Hosting.Urls.Add(uriString);
            Identifier identifier = new Identifier("Test", KeyType.Custom);
            IAssetAdministrationShellServiceProvider serviceProvider = aas.CreateServiceProvider(true);
            serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);
            aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
            aasServer.SetServiceProvider(serviceProvider);
            aasServer.RunAsync();

            repositoryService.UploadActiveTicket(uriString, structureDescriptor, statesDescriptor);
            var ticket = ticketRepository.Tickets.First();
            var message = orchestrationService.ExecuteTestPipelineByUri(ticket.Uri!.ToString());

            Assert.That(message, Is.EqualTo($"Successfully executed test pipeline for ticket with uri: {ticket.Uri}"));
            Assert.That(ticketRepository.Tickets.First().TestReport, Is.Not.Null);
        }
    }
}

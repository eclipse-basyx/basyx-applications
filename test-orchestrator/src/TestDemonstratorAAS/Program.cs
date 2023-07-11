//External libraries
using NLog.Web;
using BaSyx.AAS.Server.Http;
using BaSyx.API.Components;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.Common.UI;
using BaSyx.Common.UI.Swagger;
using BaSyx.Utils.Settings.Types;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using Microsoft.EntityFrameworkCore;

//Core components
using TestDemonstrator.Orchestrator;
using TestDemonstrator.Orchestrator.Contracts;
using TestDemonstrator.TestObjectProviders;
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestRunners;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;
using TestDemonstrator.TestSuites;

//Services
using TestDemonstratorAAS.OrchestrationService;
using TestDemonstratorAAS.OrchestrationService.Contracts;
using TestDemonstratorAAS.RepositoryService;
using TestDemonstratorAAS.RepositoryService.Contracts;

//Administration Shell
using TestDemonstratorAAS.AdministrationShell;


namespace TestDemonstratorAAS
{
    public class Program
    {
        public static void Main(string[] args)
        {
            if (args.Length == 0)
            {
                args = new string[] { "5040", "5000" };
            }

            //Reading the ports
            var httpPort = ushort.Parse(args[0]);
            var httpsPort = ushort.Parse(args[1]);
 
            //Reading the config file
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                .AddJsonFile("appsettings.json")
                .Build();

            //Specifying the DB connection
            DbContextOptions<TestTicketContext> options = new DbContextOptionsBuilder<TestTicketContext>()
                .UseSqlServer(configuration.GetConnectionString("DefaultConnection"))
                .Options;

            //Instantiating the core components
            IDescriptorFactory descriptorFactory = new DescriptorFactory();
            ITicketBuilder ticketBuilder = new TicketBuilder();
            ITicketRepository ticketRepository = new TicketDbRepository(options);

            ITestRunner testRunner = new NUnitTestRunner();
            ITestObjectProvider testObjectProvider = new TestObjectProvider(ticketRepository);
            ICollection<Type> testPipeline = new List<Type>() { typeof(StructureTests), typeof(StateMachineTests)};
            ITestOrchestrator testOrchestrator = new TestOrchestrator();

            ITicketRepositoryService repositoryService = new TicketRepositoryService(ticketRepository, ticketBuilder, descriptorFactory);
            ITestOrchestrationService orchestrationService = new TestOrchestrationService(testOrchestrator, testObjectProvider, testRunner, testPipeline);            
            TestManagementShell testManagementAAS = new TestManagementShell(orchestrationService, repositoryService);

            //Running the AAS
            RunAssetAdministrationShellServer(testManagementAAS.AdministrationShell, httpPort, httpsPort);
        }

        public static void RunAssetAdministrationShellServer(IAssetAdministrationShell aas, ushort httpPort, ushort httpsPort)
        {
            ServerSettings aasServerSettings = ServerSettings.CreateSettings();
            aasServerSettings.ServerConfig.Hosting.ContentPath = "Content";
            aasServerSettings.ServerConfig.Hosting.Environment = "Development";
            aasServerSettings.ServerConfig.Hosting.Urls.Add($"http://+:{httpPort}");
            aasServerSettings.ServerConfig.Hosting.Urls.Add($"https://+:{httpsPort}");
            aasServerSettings.Miscellaneous.Add("CompanyLogo", "/images/Bosch.png");

            IAssetAdministrationShellServiceProvider serviceProvider = aas.CreateServiceProvider(true);
            serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);

            AssetAdministrationShellHttpServer aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
            aasServer.WebHostBuilder.UseNLog();
            aasServer.SetServiceProvider(serviceProvider);
            aasServer.AddBaSyxUI(PageNames.AssetAdministrationShellServer);
            aasServer.AddSwagger(Interface.AssetAdministrationShell);
            aasServer.Run();
        }
    }
}
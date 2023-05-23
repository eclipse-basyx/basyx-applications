using BaSyx.AAS.Server.Http;

using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;

using BaSyx.Common.UI;
using BaSyx.Common.UI.Swagger;

using BaSyx.Models.Connectivity.Descriptors;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;

using BaSyx.Utils.Settings.Sections;
using BaSyx.Utils.Settings.Types;

using BaSyx.Registry.Client.Http;

using NLog.Web;


namespace OPC2AAS
{
    /// <summary>
    /// This is the main class for the OPC2AAS program.
    /// </summary>
    class Program
    {
        /// <summary>
        /// The entry point for the program.
        /// </summary>
        /// <param name="args">The command-line arguments passed to the program.</param>
        static void Main(string[] args)
        {
            // Create the AAS that is used to generate AASs and databridge config files from OPC UA data structures (from here on called "Generator AAS")
            GeneratorAAS generatorAAS = new GeneratorAAS();
            generatorAAS.CreateGeneratorAAS(); // Create AAS
            generatorAAS.AddCreationSubmodel(); // Add operation SubmodelElement to enter OPC UA Server URL
            generatorAAS.AddOutputSubmodel(); // Add SubmodelElementCollection containing generated AAS and databridge config files
            AssetAdministrationShell createdAAS = generatorAAS.GetGeneratorAAS(); // Get the created AAS

            // Create Server Settings Object
            ServerSettings serverSettings = ServerSettings.CreateSettings();
            // Add Contentpath to Serversettings
            serverSettings.ServerConfig.Hosting.ContentPath = "Content";
            // Add Environmenttype to Serversettings
            serverSettings.ServerConfig.Hosting.Environment = "Development";
            // Add AAS Port to Serversettings
            serverSettings.ServerConfig.Hosting.Urls.Add("http://+:" + 4700);
            // Instantiate Asset Administration Shell Service
            IAssetAdministrationShellServiceProvider shellService = createdAAS.CreateServiceProvider(true);

            // Dictate Asset Administration Shell service to use provided endpoints from the server configuration
            shellService.UseAutoEndpointRegistration(serverSettings.ServerConfig);

            // create the endpoint for the AAS
            BaSyx.Models.Connectivity.IEndpoint endpointAASHttp = new BaSyx.Models.Connectivity.HttpEndpoint("http://localhost:4700/aas"); // aas http endpoint
            List<BaSyx.Models.Connectivity.IEndpoint> HttpEndpoints = new List<BaSyx.Models.Connectivity.IEndpoint>
            {
                endpointAASHttp
            };
            // create AASDescriptor
            AssetAdministrationShellDescriptor AASDescriptor = new AssetAdministrationShellDescriptor(createdAAS, HttpEndpoints);

            // Create Clientconfiguration Object
            ClientConfiguration cc = new ClientConfiguration();
            // Add Endpoint configuration for the registration with the Registry AAS
            cc.Endpoint = "http://registrydb:4099/registry";
            // Add the Clientconfiguration to the Registry Client Instance
            RegistryClientSettings registryClientSettings = new RegistryClientSettings();
            registryClientSettings.ClientConfig = cc;
            registryClientSettings.RegistryConfig.RegistryUrl = "http://registrydb:4099/registry";
            // Create a Registry Client Instance
            RegistryHttpClient registryClient = new RegistryHttpClient(registryClientSettings);
            // register AAS at the Registry-Server
            registryClient.CreateOrUpdateAssetAdministrationShellRegistration(createdAAS.Identification.Id, AASDescriptor);

            // Initialize generic HTTP-REST interface passing previously loaded server configuration
            AssetAdministrationShellHttpServer server = new AssetAdministrationShellHttpServer(serverSettings);
            // Configure the entire application to use your own logger library (here: Nlog)
            server.WebHostBuilder.UseNLog();
            // Assign Asset Administration Shell Service to the generic HTTP-REST interface
            server.SetServiceProvider(shellService);
            // Add BaSyx Web UI
            server.AddBaSyxUI(PageNames.AssetAdministrationShellServer);
            // Add Swagger documentation and UI
            server.AddSwagger(Interface.AssetAdministrationShell); // TODO: fix this because Swagger currently isn't showing in the Basyx UI
            // Run HTTP server
            Console.WriteLine("Successfully created Generator AAS!");
            server.Run();
        }
    }
}

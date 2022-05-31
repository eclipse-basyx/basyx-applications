using BaSyx.Common.UI;
using BaSyx.Common.UI.Swagger;
using BaSyx.Registry.ReferenceImpl.FileBased;
using BaSyx.Registry.Server.Http;
using BaSyx.Utils.Settings.Sections;
using BaSyx.Utils.Settings.Types;
using System.Collections.Generic;


namespace Registry
{
    class Program
    {
        static void Main(string[] args)
        {
            ServerSettings registrySettings = ServerSettings.CreateSettings();
            registrySettings.ServerConfig.Hosting = new HostingConfiguration()
            {
                Urls = new List<string>()
                {
                    "http://localhost:4999",
                    "https://localhost:4499"
                },
                Environment = "Development",
                ContentPath = "Content"
            };

            RegistryHttpServer registryServer = new RegistryHttpServer(registrySettings);
            FileBasedRegistry fileBasedRegistry = new FileBasedRegistry();
            
            registryServer.SetRegistryProvider(fileBasedRegistry);
            registryServer.AddBaSyxUI(PageNames.AssetAdministrationShellRegistryServer);
            registryServer.AddSwagger(Interface.AssetAdministrationShellRegistry);
            registryServer.Run();
        }
    }
}

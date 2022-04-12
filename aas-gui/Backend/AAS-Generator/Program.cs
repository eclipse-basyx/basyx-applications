using BaSyx.AAS.Server.Http;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.API.Components;
using BaSyx.Common.UI;
using BaSyx.Common.UI.Swagger;
using BaSyx.Models.Connectivity;
using BaSyx.Models.Connectivity.Descriptors;
using BaSyx.Models.Core.AssetAdministrationShell;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Core.AssetAdministrationShell.Identification.BaSyx;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Core.Common;
using BaSyx.Registry.Client.Http;
using BaSyx.Registry.ReferenceImpl.FileBased;
using BaSyx.Registry.Server.Http;
using BaSyx.Submodel.Server.Http;
using BaSyx.Utils.Settings.Sections;
using BaSyx.Utils.Settings.Types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace AAS_Generator
{
    class Program
    {
        static void Main(string[] args)
        {
            List<string> dictList = new List<string> { "Idle", "Aborted", "Halted", "Execute" };

            for (int i = 0; i < 4; i++)
            {
                AssetAdministrationShell aas = new AssetAdministrationShell("AAS_" + i, new BaSyxShellIdentifier("AAS_" + i, "1.0.0"))
                {
                    Description = new LangStringSet()
                    {
                       new LangString("de", i + ". VWS"),
                       new LangString("en", i + ". AAS")
                    },
                    Administration = new AdministrativeInformation()
                    {
                        Version = "1.0",
                        Revision = "120"
                    },
                    Asset = new Asset("Asset_" + i, new BaSyxAssetIdentifier("Asset_" + i, "1.0.0"))
                    {
                        Kind = AssetKind.Instance,
                        Description = new LangStringSet()
                        {
                              new LangString("de", i + ". Asset"),
                              new LangString("en", i + ". Asset")
                        }
                    }
                };

                for(int j=0; j<3; j++)
                {
                    Submodel sm = new Submodel("Submodel_" + i, new BaSyxSubmodelIdentifier("Submodel_" + i, "1.0.0"));

                    RandomIntProperty p1 = new RandomIntProperty("Prop1");
                    RandomDoubleProperty p2 = new RandomDoubleProperty("Prop2");
                    RandomStringProperty p3 = new RandomStringProperty("Prop3", dictList);

                    sm.SubmodelElements.Add(p1);
                    sm.SubmodelElements.Add(p2);
                    sm.SubmodelElements.Add(p3);

                    aas.Submodels.Add(sm);
                }


                int port = 5080 + i;

                ServerSettings aasServerSettings = ServerSettings.CreateSettings();
                aasServerSettings.ServerConfig.Hosting.ContentPath = "Content";
                aasServerSettings.ServerConfig.Hosting.Environment = "Development";
                aasServerSettings.ServerConfig.Hosting.Urls.Add("http://+:" + port);

                IAssetAdministrationShellServiceProvider serviceProvider = aas.CreateServiceProvider(true);
                serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);

                ClientConfiguration cc = new ClientConfiguration();
                cc.Endpoint = "http://localhost:4999";

                RegistryHttpClient registryClient = new RegistryHttpClient();

                registryClient.Settings.ClientConfig = cc;

                registryClient.CreateOrUpdateAssetAdministrationShellRegistration(aas.Identification.Id, new AssetAdministrationShellDescriptor(aas, serviceProvider.ServiceDescriptor.Endpoints));

                AssetAdministrationShellHttpServer aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
                aasServer.SetServiceProvider(serviceProvider);
                aasServer.AddBaSyxUI(PageNames.AssetAdministrationShellServer);
                aasServer.AddSwagger(Interface.AssetAdministrationShell);
                aasServer.RunAsync();

            }

            while (true) ;
        }
    }
}

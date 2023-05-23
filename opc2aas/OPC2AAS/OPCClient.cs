using BaSyx.Models.Core.AssetAdministrationShell;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Export;
using Opc.Ua;
using Opc.Ua.Client;
using Opc.Ua.Configuration;
using System.Text.RegularExpressions;
using System.IO.Compression;

namespace OPC2AAS
{
    /// <summary>
    /// Class for the OPC UA client.
    /// </summary>
    public class OPCClient
    {
        /// <summary>
        /// OPC session
        /// </summary>
        Session? OPCSession;
        /// <summary>
        /// The root OPC UA node
        /// </summary>
        OPCUtil.Node? RootNode;
        /// <summary>
        /// The AAS to be created from the OPC UA datastructure
        /// </summary>
        AssetAdministrationShell? aas = null;
        /// <summary>
        /// Method to create the ´head o the to be generated AAS.
        /// </summary>
        /// <param name="AASName">Name of the AAS.</param>
        public void CreateAASHead(string AASName)
        {
            aas = new AssetAdministrationShell(AASName, new Identifier(AASName, KeyType.IRI))
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Automatisch generierte Verwaltungsschale aus dem OPC2AAS Tool."),
                    new LangString("en", "Automatically generated Asset Administration Shell from the OPC2AAS tool.")
                },
                Administration = new AdministrativeInformation()
                {
                    Version = "1.0",
                    Revision = "120"
                }
            };
        }
        /// <summary>
        /// Method that creates a session with the OPC UA server.
        /// </summary>
        /// <param name="OPCAddress">The OPC UA servers address.</param>
        /// <param name="ExtendedBrowse">if set to <c>true</c> [extended browse].</param>
        /// <param name="AASName">The name of the to be generated AAS.</param>
        public void RetrieveAllOPCNodes(string OPCAddress, bool ExtendedBrowse, string AASName)
        {
            Console.WriteLine("Initialize OPC connection.");

            // Create the OPC UA Configuration
            var config = new ApplicationConfiguration()
            {
                ApplicationName = "MyHomework",
                ApplicationUri = Opc.Ua.Utils.Format(@"urn:{0}:MyHomework", System.Net.Dns.GetHostName()),
                ApplicationType = ApplicationType.Client,
                SecurityConfiguration = new SecurityConfiguration
                {
                    ApplicationCertificate = new CertificateIdentifier { StoreType = @"Directory", StorePath = @"%CommonApplicationData%\OPC Foundation\CertificateStores\MachineDefault", SubjectName = "MyHomework" },
                    TrustedIssuerCertificates = new CertificateTrustList { StoreType = @"Directory", StorePath = @"%CommonApplicationData%\OPC Foundation\CertificateStores\UA Certificate Authorities" },
                    TrustedPeerCertificates = new CertificateTrustList { StoreType = @"Directory", StorePath = @"%CommonApplicationData%\OPC Foundation\CertificateStores\UA Applications" },
                    RejectedCertificateStore = new CertificateTrustList { StoreType = @"Directory", StorePath = @"%CommonApplicationData%\OPC Foundation\CertificateStores\RejectedCertificates" },
                    AutoAcceptUntrustedCertificates = true
                },
                TransportConfigurations = new TransportConfigurationCollection(),
                TransportQuotas = new TransportQuotas { OperationTimeout = 15000 },
                ClientConfiguration = new ClientConfiguration { DefaultSessionTimeout = 60000 },
                TraceConfiguration = new TraceConfiguration()
            };

            // Certificate validator
            config.Validate(ApplicationType.Client).GetAwaiter().GetResult();
            if (config.SecurityConfiguration.AutoAcceptUntrustedCertificates)
            {
                config.CertificateValidator.CertificateValidation += (s, e) => { e.Accept = (e.Error.StatusCode == Opc.Ua.StatusCodes.BadCertificateUntrusted); };
            }

            // Create a new Application Instance
            var application = new ApplicationInstance
            {
                ApplicationName = "OpenBasys",
                ApplicationType = ApplicationType.Client,
                ApplicationConfiguration = config
            };

            application.CheckApplicationInstanceCertificate(false, 2048).GetAwaiter().GetResult();

            // split the opc address into the server url and the server port
            string[] splitOPCAddress = OPCAddress.Split(":");
            string serverUrl = splitOPCAddress[0];
            int serverPort = int.Parse(splitOPCAddress[1]);

            // when the serverUrl is localhost, it has to be replaced with host.docker.internal, so that the docker container can access the host
            if (serverUrl == "localhost")
            {
                serverUrl = "host.docker.internal";
            }

            // join the server url and the server port to the endpoint url
            string endpointUrl = "opc.tcp://" + serverUrl + ":" + serverPort + "/";

            // Create the endpoint, which is on the PLC 
            var selectedEndpoint = CoreClientUtils.SelectEndpoint(endpointUrl, useSecurity: false);

            Session session = null;

            // Try creating the session based on the config
            session = OPCUtil.CreateSession(config, selectedEndpoint);

            Console.WriteLine("OPC UA Session created");
            OPCSession = session;

            // create root node
            var root = OPCUtil.getNodes(session, ExtendedBrowse);
            RootNode = root;

            // create/clear Databridge config files
            DatabridgeConfig databridgeconfig = new DatabridgeConfig();
            databridgeconfig.CreateOrClearConfigFiles();

            // create AAS from OPC UA Nodes
            CreateAASFromOPCNodes(OPCAddress, session, root, AASName);
        }
        /// <summary>
        /// Creates Submodels for all nodes right below the root node of the OPC UA server.
        /// </summary>
        /// <param name="OPCAddress">The OPC address.</param>
        /// <param name="session">The OPC session.</param>
        /// <param name="root">The root node.</param>
        /// <param name="AASName">Name of the to be generated AAS.</param>
        public void CreateAASFromOPCNodes(string OPCAddress, Session session, OPCUtil.Node root, string AASName)
        {
            // iterate over all children of the rood node
            foreach (var rootChild in root.children)
            {
                // get the name of each child (and replace all non alphanumeric characters so that the resulting Submodel is valid)
                string submodelName = ReplaceNonAlphanumeric(rootChild.name);
                // create a new submodel for each child and fill the submodel with the to be created SubmodelElements
                Submodel sm_child = GetGenericStructure<Submodel>(OPCAddress, session, rootChild, root, submodelName, AASName, "");
                // Add every created Submodel to the AAS
                aas.Submodels.Add(sm_child);
            }
        }
        /// <summary>
        /// Fills the given Submodels recursively with subordinate SubmodelElements 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="OPCAddress">The address of the OPC UA server.</param>
        /// <param name="session">The OPC session.</param>
        /// <param name="currentNode">The current node.</param>
        /// <param name="root">The root node.</param>
        /// <param name="submodelName">Name of the parent Submodel.</param>
        /// <param name="AASName">Name of the to be generated AAS.</param>
        /// <param name="idShortPath">Path for the current SubmodelElement (layer).</param>
        /// <returns></returns>
        public static T GetGenericStructure<T>(string OPCAddress, Session session, OPCUtil.Node currentNode, OPCUtil.Node root, string submodelName, string AASName, string idShortPath)
        {
            // initialize the DatabridgeConfig class used to create the config files
            DatabridgeConfig databridgeconfig = new DatabridgeConfig();

            // create an empty Submodel/SubmodelElementCollection for the current node (layer)
            Submodel sm_child;
            SubmodelElementCollection smc_child;

            // check if type of T is SubmodelElementCollection
            if (typeof(T) == typeof(SubmodelElementCollection))
            {
                // if the type is SubmodelElementCollection, add the name of the SubmodelElementCollection to the idShortPath
                idShortPath += ReplaceNonAlphanumeric(currentNode.name) + "/";
            }

            // create Submodel/SubmodElelementCollection for current Node
            sm_child = new Submodel(ReplaceNonAlphanumeric(currentNode.name), new Identifier(Guid.NewGuid().ToString(), KeyType.Custom));
            smc_child = new SubmodelElementCollection(ReplaceNonAlphanumeric(currentNode.name));

            // fill the SubmodelElementCollections with subordinate SubmodelElements as long as there are children (lowest layer is represented by only properties)
            foreach (var child in currentNode.children)
            {
                // check if the child has children itself
                if (child.children.Count > 0)
                {
                    // if yes, call the method recursively
                    SubmodelElementCollection children_collection = GetGenericStructure<SubmodelElementCollection>(OPCAddress, session, child, root, submodelName, AASName, idShortPath);
                    // add the SubmodelElementCollection to the Submodel/SubmodelElementCollection
                    sm_child.SubmodelElements.Add(children_collection);
                    smc_child.Add(children_collection);
                }
                else
                {
                    // create Databridge config files
                    // add Property to the opcuaconsumer.json file
                    databridgeconfig.WriteOpcUaConsumer(OPCAddress, child, submodelName, idShortPath);
                    // add Property to the routes.json file
                    databridgeconfig.WriteRoutes(child, submodelName, idShortPath);
                    // add Property to the aasserver.json file
                    databridgeconfig.WriteAASServer(child, submodelName, AASName, idShortPath);

                    // check if dataType is set
                    if (child.dataType != "")
                    {
                        Console.WriteLine($"Property: {child.name} with dataType: {child.dataType}");
                        // add Property using the dataType of the Node
                        if (child.dataType == "bool")
                        {
                            Property Prop = new Property<bool>(ReplaceNonAlphanumeric(child.name));
                            sm_child.SubmodelElements.Add(Prop);
                            smc_child.Add(Prop);
                        }
                        else if (child.dataType == "int")
                        {
                            Property Prop = new Property<int>(ReplaceNonAlphanumeric(child.name));
                            sm_child.SubmodelElements.Add(Prop);
                            smc_child.Add(Prop);
                        }
                        else if (child.dataType == "float")
                        {
                            Property Prop = new Property<float>(ReplaceNonAlphanumeric(child.name));
                            sm_child.SubmodelElements.Add(Prop);
                            smc_child.Add(Prop);
                        }
                        else
                        {
                            Property Prop = new Property<string>(ReplaceNonAlphanumeric(child.name));
                            sm_child.SubmodelElements.Add(Prop);
                            smc_child.Add(Prop);
                        }
                    }
                    else
                    {
                        Console.WriteLine("No dataType set for " + child.name);
                        sm_child.SubmodelElements.Add(new Property<string>(ReplaceNonAlphanumeric(child.name)) { });
                        smc_child.Add(new Property<string>(ReplaceNonAlphanumeric(child.name)) { });
                    }
                }
            }
            // check if type of T is Submodel or SubmodelElementCollection
            if (typeof(T) == typeof(Submodel))
            {
                // if the type is Submodel, return the Submodel
                return (T)Convert.ChangeType(sm_child, typeof(T));
            }
            else
            {
                // if the type is SubmodelElementCollection, return the SubmodelElementCollection
                return (T)Convert.ChangeType(smc_child, typeof(T));
            }
        }
        /// <summary>
        /// Replaces non alphanumeric characters for creating valid Submodels/SubmodelElements.
        /// </summary>
        /// <param name="input">The input string.</param>
        /// <returns>Return alphanumeric string.</returns>
        public static string ReplaceNonAlphanumeric(string input)
        {
            return Regex.Replace(input, @"[^a-zA-Z0-9]", "");
        }
        /// <summary>
        /// Converts the AAS to a XML-File (in the future AASX-File).
        /// </summary>
        public void ConvertAASToXML()
        {
            var environment = new AssetAdministrationShellEnvironment_V2_0(aas);
            string xmlFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "AAS.xml");
            if (!System.IO.File.Exists(xmlFilePath))
            {
                // create an empty AAS XML file if it does not exist
                using (FileStream fs = System.IO.File.Create(xmlFilePath)) { }
                System.IO.File.WriteAllText(xmlFilePath, "");
            }
            else
            {
                // clear the old AAS XML file
                System.IO.File.WriteAllText(xmlFilePath, "");
            }
            // write the AAS to the XML file
            environment.WriteEnvironment_V2_0(ExportType.Xml, xmlFilePath);

            // create AASX file (ZIP file) TODO: currently not working -> fix needed!
            // create AASX folder inside Content Folder if it does not exist
            string aasxDirectoryPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "aasx");
            if (!Directory.Exists(aasxDirectoryPath))
            {
                Directory.CreateDirectory(aasxDirectoryPath);
            }

            // move the xml file to the aasx folder
            string destinationXmlFilePath = Path.Combine(aasxDirectoryPath, Path.GetFileName(xmlFilePath));
            if (System.IO.File.Exists(destinationXmlFilePath)) System.IO.File.Delete(destinationXmlFilePath);
            System.IO.File.Move(xmlFilePath, destinationXmlFilePath);

            // Create the "mimetype" file with the correct content
            string mimetypeFilePath = Path.Combine(aasxDirectoryPath, "mimetype");
            if (System.IO.File.Exists(mimetypeFilePath)) System.IO.File.Delete(mimetypeFilePath);
            System.IO.File.WriteAllText(mimetypeFilePath, "application/asset-administration-shell-package");

            // Create the AASX file from the "aasx" directory
            string aasxFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "AAS.aasx");
            if (System.IO.File.Exists(aasxFilePath)) System.IO.File.Delete(aasxFilePath);
            ZipFile.CreateFromDirectory(aasxDirectoryPath, aasxFilePath);

            // Move the XML file back to its original location
            if (System.IO.File.Exists(xmlFilePath)) System.IO.File.Delete(xmlFilePath);
            System.IO.File.Move(destinationXmlFilePath, xmlFilePath);
        }
    }
}

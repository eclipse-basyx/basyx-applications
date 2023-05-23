using BaSyx.Models.Core.AssetAdministrationShell;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Core.Common;
using BaSyx.Models.Extensions;
using BaSyx.Utils.ResultHandling;


namespace OPC2AAS
{
    /// <summary>
    /// Class for the Generator AAS.
    /// </summary>
    public class GeneratorAAS
    {
        /// <summary>
        /// The Generator AAS
        /// </summary>
        private AssetAdministrationShell? AAS = null;
        /// <summary>
        /// Method to create the Generator AAS head.
        /// </summary>
        public void CreateGeneratorAAS()
        {
            AAS = new AssetAdministrationShell("OPC2AAS", new Identifier("OPC2AAS", KeyType.IRI))
            {
                Description = new LangStringSet()
                    {
                       new LangString("de", "Verwaltungsschale zum automatischen Erzeugen von VWS'en aus einer OPC UA Datenstruktur."),
                       new LangString("en", "Asset Administration Shell used for the automatic creation of AASs from an OPC UA datastructure.")
                    },
                Administration = new AdministrativeInformation()
                {
                    Version = "1.0",
                    Revision = "120"
                }
            };
        }
        /// <summary>
        /// Method that adds a Submodel to the Generator AAS which uses an operation to create an AAS from an OPC UA datastructure.
        /// </summary>
        public void AddCreationSubmodel()
        {
            Submodel creationSubmodel = new Submodel("CreationSubmodel", new Identifier("CreationSubmodel", KeyType.IRI))
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Teilmodell, dass Operationen zum Erzeugen von Verwaltungsschalen enthält."),
                    new LangString("en", "Submodel containing Operations to create Asset Administration Shells.")
                },
                Identification = new Identifier("CreationSubmodel", KeyType.IRI)
            };
            creationSubmodel.SemanticId = new Reference(new GlobalKey(KeyElements.Submodel, KeyType.IRI, "https://htw-berlin.de/create-ass-from-opc"));
            Operation generatorOperation = new Operation("CreateAASFromOPC")
            {
                Description = new LangStringSet()
                {
                    new LangString("en", "Initiates the automated AAS creation process."),
                    new LangString("de", "Startet den automatisierten VWS Generier-Prozess.")
                },
                InputVariables = new OperationVariableSet()
                {
                    new Property<string>("AAS Name")
                    {
                        Description = new LangStringSet()
                        {
                            new LangString("de", "Name der resultierenden VWS."),
                            new LangString("en", "Name of the AAS to be generated.")
                        }
                    },
                    new Property<string>("OPC Address")
                    {
                        Description = new LangStringSet()
                        {
                            new LangString("de", "Adresse des OPC UA Servers."),
                            new LangString("en", "Address of the OPC UA server.")
                        }
                    },
                    new Property<bool>("Do extended OPC Browse?")
                    {
                        Description = new LangStringSet()
                        {
                            new LangString("de", "Aktivieren Sie diese Option, wenn OPC Knoten in der generierten VWS fehlen."),
                            new LangString("en", "Activate if OPC Nodes are missing in the AAS.")
                        }
                    }
                },
                OnMethodCalled = (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                {
                    Console.WriteLine("Creation called");
                    // get Parameters from Operation
                    string? AASName = inArgs["AAS Name"]?.GetValue<string>();
                    string? OPCAddress = inArgs["OPC Address"]?.GetValue<string>();
                    bool ExtendedBrowse = (bool)(inArgs["Do extended OPC Browse?"].GetValue<bool>());

                    // Check if an AASName was provided
                    if (AASName == null || AASName == "")
                    {
                        Console.WriteLine("No AAS Name was provided!");
                        return new OperationResult(false, new Message(MessageType.Error, "No AAS name was provided!"));
                    }
                    // Check if an OPCAddress was provided
                    if (OPCAddress == null || OPCAddress == "")
                    {
                        Console.WriteLine("No OPC Address was provided!");
                        return new OperationResult(false, new Message(MessageType.Error, "No OPC address was provided!"));
                    }

                    OPCClient opcClient = new OPCClient();
                    try
                    {
                        // Create head of the AAS to be generated
                        opcClient.CreateAASHead(AASName);
                        // Retrieve Datastructure from OPC UA Server and create AAS from it
                        opcClient.RetrieveAllOPCNodes(OPCAddress, ExtendedBrowse, AASName);
                        // Create XML-File from AAS (this will be zipped into an aasx file in a future implementation)
                        opcClient.ConvertAASToXML();

                        // return success
                        Console.WriteLine("AAS was successfully created");
                        return new OperationResult(true);
                    }
                    catch (Exception ex)
                    {
                        // return error
                        Console.WriteLine("AAS could not be created! " + ex);
                        return new OperationResult(false, new Message(MessageType.Error, "AAS could not be created! " + ex));
                    }
                }

            };
            // Add Operation to Submodel
            creationSubmodel.SubmodelElements.Add(generatorOperation);
            AAS?.Submodels.Add(creationSubmodel);
        }
        /// <summary>
        /// Method that adds an output Submodel to the Generator AAS which contains the to be generated AAS (XML file) and configuration files for the Databridge (JSON files).
        /// </summary>
        public void AddOutputSubmodel()
        {
            Submodel outputSubmodel = new Submodel("OutputSubmodel", new Identifier("OutputSubmodel", KeyType.IRI))
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Teilmodell, dass erzeugte Verwaltungsschalen und Konfigurationsdateien für die Databridge enthält."),
                    new LangString("en", "Submodel containing created Asset Administration Shells and configuration files for the Databridge.")
                },
                Identification = new Identifier("CreationSubmodel", KeyType.IRI)
            };
            outputSubmodel.SemanticId = new Reference(new GlobalKey(KeyElements.Submodel, KeyType.IRI, "https://htw-berlin.de/output-generated-files"));
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File GeneratedAAS = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("GeneratedAAS")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die erzeugte Verwaltungsschale im XML-Format."),
                    new LangString("en", "The created Asset Administration Shell in XML-Format.")
                },
                MimeType = "application/xml",
                Value = "/AAS.xml"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File OPCUAConsumerFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("OPCUAConsumerFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den OPC UA Consumer."),
                    new LangString("en", "The configuration file for the OPC UA Consumer.")
                },
                MimeType = "application/json",
                Value = "/opcuaconsumer.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File ExtractValueFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("ExtractValueFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den Extract Value Transformer."),
                    new LangString("en", "The configuration file for the Extract Value Transformer.")
                },
                MimeType = "application/json",
                Value = "/jsonataExtractValue.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File JsonataTransformerFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("JsonataTransformerFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den Jsonata Transformer."),
                    new LangString("en", "The configuration file for the Jsonata Transformer.")
                },
                MimeType = "application/json",
                Value = "/jsonatatransformer.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File JsonJacksonTransformerFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("JsonJacksonTransformerFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den Json Jackson Transformer."),
                    new LangString("en", "The configuration file for the Json Jackson Transformer.")
                },
                MimeType = "application/json",
                Value = "/jsonjacksontransformer.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File AASServerFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("AASServerFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den AAS Server."),
                    new LangString("en", "The configuration file for the AAS Server.")
                },
                MimeType = "application/json",
                Value = "/aasserver.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File RoutesFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("RoutesFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für die Routen."),
                    new LangString("en", "The configuration file for the routes.")
                },
                MimeType = "application/json",
                Value = "/routes.json"
            };
            BaSyx.Models.Core.AssetAdministrationShell.Implementations.File TimerConsumerFile = new BaSyx.Models.Core.AssetAdministrationShell.Implementations.File("TimerConsumerFile")
            {
                Description = new LangStringSet()
                {
                    new LangString("de", "Die Konfigurationsdatei für den Timer Consumer."),
                    new LangString("en", "The configuration file for the Timer Consumer.")
                },
                MimeType = "application/json",
                Value = "/timerconsumer.json"
            };
            // Add Files to Submodel
            outputSubmodel.SubmodelElements.Add(GeneratedAAS);
            outputSubmodel.SubmodelElements.Add(OPCUAConsumerFile);
            outputSubmodel.SubmodelElements.Add(ExtractValueFile);
            outputSubmodel.SubmodelElements.Add(JsonataTransformerFile);
            outputSubmodel.SubmodelElements.Add(JsonJacksonTransformerFile);
            outputSubmodel.SubmodelElements.Add(AASServerFile);
            outputSubmodel.SubmodelElements.Add(RoutesFile);
            outputSubmodel.SubmodelElements.Add(TimerConsumerFile);
            // Add Submodel to Generator AAS
            AAS?.Submodels.Add(outputSubmodel);
        }
        /// <summary>
        /// Method to retrieve the Generator AAS.
        /// </summary>
        /// <returns>The Generator AAS</returns>
        /// <exception cref="System.InvalidOperationException">The requested AssetAdministrationShell does not exist.</exception>
        public AssetAdministrationShell GetGeneratorAAS() {
            if (AAS != null)
            {
                return AAS;
            }
            else
            {
                throw new InvalidOperationException("The requested AssetAdministrationShell does not exist.");
            }
        }
    }
}

using Newtonsoft.Json;

namespace OPC2AAS
{
    /// <summary>
    /// Class for the Databridge config creation
    /// </summary>
    public class DatabridgeConfig
    {
        /// <summary>
        /// Method that reates new Databridge config files and clears old ones.
        /// </summary>
        public void CreateOrClearConfigFiles()
        {
            // check if config files exist in Content Folder
            // Files: aasserver.json, jsonataExtractValue.json, jsonatatransformer.json, jsonjacksontransformer.json, opcuaconsumer.json, routes.json, timerconsumer.json
            string[] files = { "aasserver.json", "jsonataExtractValue.json", "jsonatatransformer.json", "jsonjacksontransformer.json", "opcuaconsumer.json", "routes.json", "timerconsumer.json" };
            foreach (string file in files)
            {
                string filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", file);
                // create the config file if it does not exist
                if (!File.Exists(filePath))
                {
                    using (FileStream fs = File.Create(filePath)) { }
                    
                }
                // write the respective content to the config file (empty array or default values)
                if (file == "jsonataExtractValue.json")
                {
                    File.WriteAllText(filePath, "value.value");
                }
                else if (file == "jsonjacksontransformer.json")
                {
                    File.WriteAllText(filePath, "[\r\n  {\r\n    \"uniqueId\": \"dataValueToJson\",\r\n    \"operation\": \"marshal\",\r\n    \"jacksonModules\": \"com.fasterxml.jackson.datatype.jsr310.JavaTimeModule\"\r\n  }\r\n]");
                }
                else if (file == "jsonatatransformer.json")
                {
                    File.WriteAllText(filePath, "[\r\n  {\r\n    \"uniqueId\": \"jsonataExtractValue\",\r\n    \"queryPath\": \"jsonataExtractValue.json\",\r\n    \"inputType\": \"JsonString\",\r\n    \"outputType\": \"JsonString\"\r\n  }\r\n]");
                }
                else if (file == "timerconsumer.json")
                {
                    File.WriteAllText(filePath, "[\r\n  {\r\n    \"uniqueId\": \"timer1\",\r\n    \"fixedRate\": true,\r\n    \"delay\": 0,\r\n    \"period\": 1000\r\n  }\r\n]");
                }
                else
                {
                    File.WriteAllText(filePath, "[]");
                }
            }
        }
        /// <summary>
        /// Writes the OPC UA consumer Databridge config file.
        /// </summary>
        /// <param name="OPCAddress">The OPC address.</param>
        /// <param name="currentNode">The current node.</param>
        /// <param name="submodelName">Name of the parent Submodel.</param>
        /// <param name="idShortPath">Path for the current SubmodelElement (layer).</param>
        public void WriteOpcUaConsumer(string OPCAddress, OPCUtil.Node currentNode, string submodelName, string idShortPath)
        {
            string[] parts = OPCAddress.Split(':');
            string serverUrl = parts[0];
            // change localhost to host.docker.internal (needed when running in a Docker container)
            if (serverUrl == "localhost")
            {
                serverUrl = "host.docker.internal";
            }
            int serverPort = int.Parse(parts[1]);

            string filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "opcuaconsumer.json");
            string jsonContent = File.ReadAllText(filePath);

            List<OpcEntry> entries = JsonConvert.DeserializeObject<List<OpcEntry>>(jsonContent);

            OpcEntry newEntry = new OpcEntry
            {
                uniqueId = "source://" + submodelName + "/" + idShortPath + currentNode.name,
                serverUrl = serverUrl,
                serverPort = serverPort,
                pathToService = "",
                nodeInformation = currentNode.nodeId,
                requestPublishingInterval = 50
            };
            entries.Add(newEntry);

            string newJsonContent = JsonConvert.SerializeObject(entries, Formatting.Indented);

            File.WriteAllText(filePath, newJsonContent);
        }
        /// <summary>
        /// Writes the routes Databridge config file.
        /// </summary>
        /// <param name="currentNode">The current node.</param>
        /// <param name="submodelName">Name of the parent Submodel.</param>
        /// <param name="idShortPath">Path for the current SubmodelElement (layer).</param>
        public void WriteRoutes(OPCUtil.Node currentNode, string submodelName, string idShortPath)
        {
            string filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "routes.json");
            string jsonContent = File.ReadAllText(filePath);

            List<RouteEntry> entries = JsonConvert.DeserializeObject<List<RouteEntry>>(jsonContent);
            
            RouteEntry newEntry = new RouteEntry
            {
                datasource = "source://" + submodelName + "/" + idShortPath + currentNode.name,
                transformers = new string[] { "dataValueToJson", "jsonataExtractValue" },
                datasinks = new string[] { "sink://" + submodelName + "/" + idShortPath + currentNode.name },
                trigger = "timer",
                triggerData = new TriggerData
                {
                    timerName = "timer1"
                }
            };

            entries.Add(newEntry);

            string newJsonContent = JsonConvert.SerializeObject(entries, Formatting.Indented);

            File.WriteAllText(filePath, newJsonContent);
        }
        /// <summary>
        /// Writes the aas server.
        /// </summary>
        /// <param name="currentNode">The current node.</param>
        /// <param name="submodelName">Name of the parent Submodel.</param>
        /// <param name="AASName">Name of the to be generated AAS.</param>
        /// <param name="idShortPath">Path for the current SubmodelElement (layer).</param>
        public void WriteAASServer(OPCUtil.Node currentNode, string submodelName, string AASName, string idShortPath)
        {
            string filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Content", "aasserver.json");
            string jsonContent = File.ReadAllText(filePath);

            List<AasEntry> entries = JsonConvert.DeserializeObject<List<AasEntry>>(jsonContent);

            AasEntry newEntry = new AasEntry
            {
                uniqueId = "sink://" + submodelName + "/" + idShortPath + currentNode.name,
                submodelEndpoint = "http://host.docker.internal:4500/aasServer/shells/" + AASName + "/aas/submodels/" + submodelName + "/submodel",
                idShortPath = idShortPath + currentNode.name
            };

            entries.Add(newEntry);

            string newJsonContent = JsonConvert.SerializeObject(entries, Formatting.Indented);

            File.WriteAllText(filePath, newJsonContent);
        }
    }
    /// <summary>
    /// Class for the OPC UA consumer datastructure.
    /// </summary>
    class OpcEntry
    {
        public string uniqueId { get; set; }
        public string serverUrl { get; set; }
        public int serverPort { get; set; }
        public string pathToService { get; set; }
        public string nodeInformation { get; set; }
        public int requestPublishingInterval { get; set; }
    }
    /// <summary>
    /// Class for the routes datastructure.
    /// </summary>
    class RouteEntry
    {
        public string datasource { get; set; }
        public string[] transformers { get; set; }
        public string[] datasinks { get; set; }
        public string trigger { get; set; }
        public TriggerData triggerData { get; set; }
    }
    /// <summary>
    /// Class for the trigger datastructure.
    /// </summary>
    public class TriggerData
    {
        public string timerName { get; set; }
    }
    /// <summary>
    /// Class for the AAS server datastructure.
    /// </summary>
    public class AasEntry
    {
        public string uniqueId { get; set; }
        public string submodelEndpoint { get; set; }
        public string idShortPath { get; set; }
    }
}

using Opc.Ua; 
using Opc.Ua.Client;

namespace OPC2AAS
{
    /// <summary>
    /// Contains utility functions for OPC UA communication.
    /// </summary>
    public class OPCUtil
    {
        /// <summary>
        /// Utility class for representing the OPC UA nodes in a tree structure.
        /// </summary>
        public class Node
        {
            /// <summary>
            /// Contains the children nodes of the current node.
            /// </summary>
            public LinkedList<Node> children { get; set; }
            /// <summary>
            /// The data type of the node.
            /// </summary>
            public string type { get; set; }
            /// <summary>
            /// The node name
            /// </summary>
            public string name { get; set; }
            /// <summary>
            /// The node ID
            /// </summary>
            public string nodeId { get; set; }
            /// <summary>
            /// Datatype of the node
            /// </summary>
            public string dataType { get; set; }
            /// <summary>
            /// Initializes a new instance of the <see cref="Node"/> class.
            /// </summary>
            /// <param name="type">The type of the node.</param>
            /// <param name="name">The name of the node.</param>
            /// <param name="nodeId">The node identifier.</param>
            /// <param name="dataType">Datatype of the data in the node.</param>
            public Node(string type, string name, string nodeId, string dataType="None")
            {
                this.type = type;
                this.name = name;
                this.nodeId = nodeId;
                this.children = new LinkedList<Node>();
                this.dataType = dataType;
            }
            /// <summary>
            /// Returns a reference to a child node based on its name.
            /// </summary>
            /// <param name="name">The child's name</param>
            /// <returns>A reference to a child with that name. Null if it doesn't have a child with that name.</returns>
            public Node getChildByName(string name)
            {
                foreach (Node child in children)
                    if (child.name == name)
                        return child;

                return null;
            }
        }
        /// <summary>
        /// Creates a tree structure that represents the OPC UA Server.
        /// It represents the data that is stored there. 
        /// It prunes the tree so that it includes only the Objects Folder from the Root nodel 
        /// </summary>
        /// <param name="session">The OPC UA session to the server.</param>
        /// <returns>The root node of the tree</returns>
        public static Node getNodes(Session session, bool ExtendedBrowse)
        {
            // Create a dummy root node
            Node root = new Node("", "root", "");
            // Objects that are filtered from the first layer of the OPC UA Server tree
            HashSet<String> filterObjects = new HashSet<string>() {"Server"};
            // Explore the root objects
            ReferenceDescriptionCollection refs;
            Byte[] cp;
            session.Browse(null, null, ObjectIds.ObjectsFolder, 0u, BrowseDirection.Forward, ReferenceTypeIds.HierarchicalReferences, true, (uint)NodeClass.Variable | (uint)NodeClass.Object | (uint)NodeClass.Method, out cp, out refs);

            // Create the nodes on the initial level, and create their respective subtrees.
            foreach (var rd in refs)
            {
                if (filterObjects.Contains(rd.DisplayName.Text))
                    continue;

                Node newNode = new Node("", rd.DisplayName.Text, "");
                root.children.AddLast(newNode);
                _getNodes(session, newNode, rd.NodeId, ExtendedBrowse);
            }

            return root;
        }
        /// <summary>
        /// A helper method for creating the tree structure represented in the OPC UA server.
        /// It uses the appropriate Browse calls for that. 
        /// </summary>
        /// <param name="session">The current OPC UA session</param>
        /// <param name="parent">The parent node. This is where the node and its children will be added</param>
        /// <param name="parentNodeId">The parent node ID. It's needed for the browsing</param>
        private static void _getNodes(Session session, Node parent, ExpandedNodeId parentNodeId, bool ExtendedBrowse)
        {
            // Browse the parent node's children
            ReferenceDescriptionCollection nextRefs;
            byte[] nextCp;
            session.Browse(null, null, ExpandedNodeId.ToNodeId(parentNodeId, session.NamespaceUris), 0u, BrowseDirection.Forward, ReferenceTypeIds.HierarchicalReferences, true, (uint)NodeClass.Variable | (uint)NodeClass.Object | (uint)NodeClass.Method, out nextCp, out nextRefs);

            if (ExtendedBrowse)
            {
                ReferenceDescriptionCollection nextRefs2;
                session.Browse(null, null, ExpandedNodeId.ToNodeId(parentNodeId, session.NamespaceUris), 0u, BrowseDirection.Forward, ReferenceTypeIds.Organizes, true, (uint)NodeClass.Variable | (uint)NodeClass.Object | (uint)NodeClass.Method, out nextCp, out nextRefs2);

                // join nextRefs and nextRefs2
                foreach (var nextRd in nextRefs2)
                {
                    nextRefs.Add(nextRd);
                }
            }

            // Create the children nodes
            foreach (var nextRd in nextRefs)
            {
                string datatype = "";
                
                Console.WriteLine("Adding child: " + nextRd.DisplayName.Text + ", Reference Type Id: " + nextRd.TypeDefinition.IdType);
                try
                {
                    var testval = session.ReadValue(new NodeId(nextRd.NodeId.ToString()));
                    if(testval.Value != null)
                    {
                        if (testval.Value.GetType() == typeof(Boolean))
                            datatype = "bool";
                        else if (testval.Value.GetType() == typeof(String))
                            datatype = "string";
                        else if (testval.Value.GetType() == typeof(int) || testval.Value.GetType() == typeof(Int16) || testval.Value.GetType() == typeof(Int32) || testval.Value.GetType() == typeof(Int64) || testval.Value.GetType() == typeof(UInt16) || testval.Value.GetType() == typeof(UInt32) || testval.Value.GetType() == typeof(UInt64))
                            datatype = "int";
                        else if (testval.Value.GetType() == typeof(float) || testval.Value.GetType() == typeof(Double))
                            datatype = "float";
                        else if (testval.Value.GetType() == typeof(Boolean[]))
                        {
                            Boolean[] arr = (Boolean[])testval.Value;
                        
                            datatype = "boolArray" + arr.Length.ToString();
                        }
                        else if (testval.Value.GetType() == typeof(String[]))
                        {
                            String[] arr = (String[])testval.Value;

                            datatype = "stringArray" + arr.Length.ToString();
                        }
                        else if (testval.Value.GetType() == typeof(int[]))
                        {
                            int[] arr = (int[])testval.Value;

                            datatype = "intArray" + arr.Length.ToString();
                        }
                        else if (testval.Value.GetType() == typeof(float[]))
                        {
                            float[] arr = (float[])testval.Value;

                            datatype = "floatArray" + arr.Length.ToString();
                        }
                    }
                }
                catch (Exception)
                {
                    // continue
                }

                Node newNode = new Node("", nextRd.DisplayName.Text, nextRd.NodeId.ToString(), datatype);
                
                // Create their respective subtrees
                parent.children.AddLast(newNode);

                _getNodes(session, newNode, nextRd.NodeId, ExtendedBrowse);
            }
        }
        /// <summary>
        /// Finds the node.
        /// </summary>
        /// <param name="session">The session.</param>
        /// <param name="root">The root.</param>
        /// <param name="name">The name.</param>
        /// <param name="output">The output.</param>
        private static void _findNode(Session session, Node root, string name, ref Node output)
        {
            if(root != null)
            {
                if (root.name == name)
                    output = root;

                foreach (var it in root.children)
                    _findNode(session, it, name, ref output);
            }
        }
        /// <summary>
        /// Creates the session.
        /// </summary>
        /// <param name="config">The configuration.</param>
        /// <param name="selectedEndpoint">The selected endpoint.</param>
        /// <returns></returns>
        public static Session CreateSession(ApplicationConfiguration config, EndpointDescription selectedEndpoint)
        {
            Console.WriteLine("Creating session...");
            // try to create a session indefinitely until it is successfull
            try
            {
                return Session.Create(config, new ConfiguredEndpoint(null, selectedEndpoint, EndpointConfiguration.Create(config)), false, "", 60000, null, null).GetAwaiter().GetResult();
            }
            catch
            {
                // Wait for 10 seconds before trying again
                System.Threading.Thread.Sleep(10000);
                return CreateSession(config, selectedEndpoint);
            }
        }
    }
}

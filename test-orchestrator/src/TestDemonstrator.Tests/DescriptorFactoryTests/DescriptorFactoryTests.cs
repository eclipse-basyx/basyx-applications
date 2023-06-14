//#define TEST_DATA_CLASSIFICATION 

using System.Xml;

using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Export;

using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Tests.DescriptorFactoryTests
{
    public class DescriptorFactoryTests
    {

        private IDescriptorFactory factory = null!;
        private static string root = @"../../../DescriptorFactoryTests/TestData";

        [SetUp]
        public void Setup()
        {
            factory = new DescriptorFactory();
        }

#if TEST_DATA_CLASSIFICATION
        // Test data classification - would fail
        // The input data indicating a successful execution should be used
        // for all positive test scenarios. 
        // The input data indicating a failure should be used for
        // all negative test scenarios.
        private static string[] InputDataClassification_ValidAndInvalidContent =
        {
             //internal shells
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
             $@"{root}/PassiveShells/HTW/02_WorkspaceExtension_AAS.xml",
             $@"{root}/PassiveShells/HTW/03_BucklingArmRobot_AAS.xml",
             $@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml",
             $@"{root}/PassiveShells/HTW/05_SupplyUnit_AAS.xml",

             //https://admin-shell-io.com/samples/
             $@"{root}/PassiveShells/IDTA/01_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/02_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/03_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/04_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/05_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/06_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/07_PhoenixContact_AAS.xml",
             $@"{root}/PassiveShells/IDTA/08_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/09_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/10_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/11_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/12_Pepperl+Fuchs_AAS.xml",
             $@"{root}/PassiveShells/IDTA/13_DKE_AAS.xml",
             $@"{root}/PassiveShells/IDTA/14_Siemens_AAS.xml",
             $@"{root}/PassiveShells/IDTA/15_Siemens_AAS.xml",
             $@"{root}/PassiveShells/IDTA/16_Lenze_AAS.xml",
             $@"{root}/PassiveShells/IDTA/17_ABB_AAS.xml",
             $@"{root}/PassiveShells/IDTA/18_Hitachi_AAS.xml",
             $@"{root}/PassiveShells/IDTA/19_Balluff_AAS.xml",
             $@"{root}/PassiveShells/IDTA/20_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/21_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/22_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/23_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/24_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/25_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/26_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/27_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/30_Wittenstein_AAS.xml",
             $@"{root}/PassiveShells/IDTA/31_SICK_AAS.xml",
             $@"{root}/PassiveShells/IDTA/32_SICK_AAS.xml",
             $@"{root}/PassiveShells/IDTA/33_SICK_AAS.xml",
             $@"{root}/PassiveShells/IDTA/34_Festo_AAS.xml",
         };
        [TestCaseSource(nameof(InputDataClassification_ValidAndInvalidContent))]
        public void AdministrationShell_ShouldBeAcceptedFromSolution(string path)
        {

            string content = System.IO.File.ReadAllText(path);
            var env = AssetAdministrationShellEnvironment_V2_0.ReadEnvironment_V2_0(path);
            Assert.IsNotNull(env);
        }
#endif
        
         //TC.DF01
         private static string[] CreatePassiveShell_DeserializableContent = 
         {
             //internal shells
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
             $@"{root}/PassiveShells/HTW/02_WorkspaceExtension_AAS.xml",
             $@"{root}/PassiveShells/HTW/03_BucklingArmRobot_AAS.xml",
             //$@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml", //unreadable - see classification results
             $@"{root}/PassiveShells/HTW/05_SupplyUnit_AAS.xml",

             //https://admin-shell-io.com/samples/
             $@"{root}/PassiveShells/IDTA/01_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/02_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/03_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/04_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/05_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/06_Bosch_AAS.xml",
             $@"{root}/PassiveShells/IDTA/07_PhoenixContact_AAS.xml",
             $@"{root}/PassiveShells/IDTA/08_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/09_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/10_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/11_SchneiderElectric_AAS.xml",
             $@"{root}/PassiveShells/IDTA/12_Pepperl+Fuchs_AAS.xml",
             //$@"{root}/PassiveShells/IDTA/13_DKE_AAS.xml", //unreadable - see classification results
             $@"{root}/PassiveShells/IDTA/14_Siemens_AAS.xml",
             $@"{root}/PassiveShells/IDTA/15_Siemens_AAS.xml",
             $@"{root}/PassiveShells/IDTA/16_Lenze_AAS.xml",
             //$@"{root}/PassiveShells/IDTA/17_ABB_AAS.xml", //unreadable - see classification results
             //$@"{root}/PassiveShells/IDTA/18_Hitachi_AAS.xml", //unreadable - see classification results
             //$@"{root}/PassiveShells/IDTA/19_Balluff_AAS.xml", //unreadable - see classification results
             $@"{root}/PassiveShells/IDTA/20_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/21_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/22_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/23_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/24_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/25_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/26_Festo_AAS.xml",
             $@"{root}/PassiveShells/IDTA/27_Festo_AAS.xml",
             //$@"{root}/PassiveShells/IDTA/30_Wittenstein_AAS.xml", //unreadable - see classification results
             $@"{root}/PassiveShells/IDTA/31_SICK_AAS.xml",
             $@"{root}/PassiveShells/IDTA/32_SICK_AAS.xml",
             $@"{root}/PassiveShells/IDTA/33_SICK_AAS.xml",
             //$@"{root}/PassiveShells/IDTA/34_Festo_AAS.xml", //unreadable - see classification results
         };
         [TestCaseSource(nameof(CreatePassiveShell_DeserializableContent))]
         public void CreatePassiveShell_WithDeserializableContent_ReturnsValidHandler(string path)
         {
             string content = System.IO.File.ReadAllText(path);
             IAssetAdministrationShell? shell = factory.CreatePassiveShell(content);
             Assert.IsNotNull(shell);
         }

         //TC.DF02
         private static string[] CreatePassiveShell_FullyValidContent =
         {
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
         };
         [TestCaseSource(nameof(CreatePassiveShell_FullyValidContent))]
         public void CreatePassiveShell_WithDeserializableContent_ReturnsSameContentAfterDeserialization(string path)
         {
             string expected = System.IO.File.ReadAllText(path);
             var xmlDoc = new XmlDocument();
             xmlDoc.LoadXml(expected);

             IAssetAdministrationShell? shell = factory.CreatePassiveShell(expected);
             string current = factory.CreatePassiveShell(shell);
             Assert.IsTrue(xmlDoc.InnerXml == current);
         }

         //TC.DF03
         private static string[] CreatePassiveShell_UndeserializableContent =
         {
             $@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml",
             $@"{root}/PassiveShells/IDTA/13_DKE_AAS.xml",
             $@"{root}/PassiveShells/IDTA/17_ABB_AAS.xml",
             $@"{root}/PassiveShells/IDTA/18_Hitachi_AAS.xml",
             $@"{root}/PassiveShells/IDTA/19_Balluff_AAS.xml",
             $@"{root}/PassiveShells/IDTA/30_Wittenstein_AAS.xml",
             $@"{root}/PassiveShells/IDTA/34_Festo_AAS.xml",
         };
         [TestCaseSource(nameof(CreatePassiveShell_UndeserializableContent))]
         public void CreatePassiveShell_WithUndeserializableContent_ThrowsException(string path)
         {
             string content = System.IO.File.ReadAllText(path);
             Assert.Throws<NullReferenceException>(() => factory.CreatePassiveShell(content));
         }

         //TC.DF04
         private static string[] CreatePassiveShell_SerializableInstance =
         {
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
         };
         [TestCaseSource(nameof(CreatePassiveShell_SerializableInstance))]
         public void CreatePassiveShell_WithSerializableInstance_ReturnsSameContentAfterSerialization(string path)
         {
             string inputContent = System.IO.File.ReadAllText(path);
             var xmlDoc = new XmlDocument();
             xmlDoc.LoadXml(inputContent);
             var instance = factory.CreatePassiveShell(inputContent);

             string outputContent = factory.CreatePassiveShell(instance!);

             Assert.That(outputContent == xmlDoc.InnerXml);
         }

         //TC.DF05
         private static IAssetAdministrationShell?[] CreatePassiveShell_UnserializableInstance =
         {
             null,
             new AssetAdministrationShell(null, null),
         };
         [TestCaseSource(nameof(CreatePassiveShell_UnserializableInstance))]
         public void CreatePassiveShell_WithUnserializableInstance_ThrowsException(IAssetAdministrationShell shell)
         {
             Assert.Throws<NullReferenceException>(() => factory.CreatePassiveShell(shell));
         }

         //TC.DF06
         private static string[] CreateStructureDescriptor_DeserializableContent =
         {
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
         };
         [TestCaseSource(nameof(CreateStructureDescriptor_DeserializableContent))]
         public void CreateStructureDescriptor_WithDeserializableContent_ReturnsValidHandler(string path)
         {
             string content = System.IO.File.ReadAllText(path);
             IStructureDescriptor descriptor = factory.CreateStructureDescriptor(content);
             Assert.IsNotNull(descriptor);
             Assert.IsNotNull(descriptor.Instance);
         }

         //TC.DF07
         [TestCaseSource(nameof(CreateStructureDescriptor_DeserializableContent))]
         public void CreateStructureDescriptor_WithDeserializableContent_ReturnsSameContentAfterDeserialization(string path)
         {
             string expected = System.IO.File.ReadAllText(path);
             var xmlDoc = new XmlDocument();
             xmlDoc.LoadXml(expected);

             IStructureDescriptor shell = factory.CreateStructureDescriptor(expected);
             string current = factory.CreateStructureDescriptor(shell.Instance);
             Assert.IsTrue(xmlDoc.InnerXml == current);
         }

         //TC.DF08
         private static string[] CreateStructureDescriptor_UndeserializableContent =
        {
            $@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml",
            $@"{root}/PassiveShells/IDTA/13_DKE_AAS.xml",
            $@"{root}/PassiveShells/IDTA/17_ABB_AAS.xml",
            $@"{root}/PassiveShells/IDTA/18_Hitachi_AAS.xml",
            $@"{root}/PassiveShells/IDTA/19_Balluff_AAS.xml",
            $@"{root}/PassiveShells/IDTA/30_Wittenstein_AAS.xml",
            $@"{root}/PassiveShells/IDTA/34_Festo_AAS.xml",
        };
         [TestCaseSource(nameof(CreateStructureDescriptor_UndeserializableContent))]
         public void CreateStructureDescriptor_WithUndeserializableContent_ThrowsException(string path)
         {
            string content = System.IO.File.ReadAllText(path);
            Assert.Throws<NullReferenceException>(() => factory.CreateStructureDescriptor(content));
         }

         //TC.DF09
         private static string[] CreateStructureDescriptor_SerializableInstance =
         {
             $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
         };
         [TestCaseSource(nameof(CreateStructureDescriptor_SerializableInstance))]
         public void CreateStructureDescriptor_WithSerializableInstance_ReturnsSameConentAfterSerialization(string path)
         {
             string inputContent = System.IO.File.ReadAllText(path);
             var xmlDoc = new XmlDocument();
             xmlDoc.LoadXml(inputContent);
             var descriptor = factory.CreateStructureDescriptor(inputContent);

             string outputContent = factory.CreateStructureDescriptor(descriptor.Instance);

             Assert.That(outputContent == xmlDoc.InnerXml);
         }

         //TC.DF10
         private static IAssetAdministrationShell?[] CreateStructureDescriptor_UnserializableInstance =
         {
             null,
             new AssetAdministrationShell(null, null),
         };
         [TestCaseSource(nameof(CreateStructureDescriptor_UnserializableInstance))]
         public void CreateStructureDescriptor_WithUnserializableInstance_ThrowsException(IAssetAdministrationShell shell)
         {
             Assert.Throws<NullReferenceException>(() => factory.CreateStructureDescriptor(shell));
         }

         //TC.DF11
         private static string[] CreateStateMachineDescriptor_DeserializableContent =
         {
              $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml",
         };
         [TestCaseSource(nameof(CreateStateMachineDescriptor_DeserializableContent))]
         public void CreateStateMachineDescriptor_WithDeserializableContent_ReturnsValidHandler(string path)
         { 
             var content = System.IO.File.ReadAllText(path);
             IStateMachineDescriptor? descriptor = factory.CreateStateMachineDescriptor(content);
             Assert.IsNotNull(descriptor);
         }

        //TC.DF12
        [TestCaseSource(nameof(CreateStateMachineDescriptor_DeserializableContent))]
        public void CreateStateMachineDescriptor_WithDeserializableContent_ReturnsSameContentAfterDeserialization(string path)
        {
            string expected = System.IO.File.ReadAllText(path);
            var xmlDoc = new XmlDocument();
            xmlDoc.LoadXml(expected);
            xmlDoc.DocumentElement!.RemoveAttribute("xmlns:xsi");
            xmlDoc.DocumentElement.RemoveAttribute("xmlns:xsd");
            xmlDoc.DocumentElement.RemoveAttribute("xsi:noNamespaceSchemaLocation");
            IStateMachineDescriptor? descriptor = factory.CreateStateMachineDescriptor(expected);

            string current = factory.CreateStateMachineDescriptor(descriptor);
            var xmlDoc3 = new XmlDocument();
            xmlDoc3.LoadXml(current);
            xmlDoc3.DocumentElement!.RemoveAttribute("xmlns:xsi");
            xmlDoc3.DocumentElement.RemoveAttribute("xmlns:xsd");
            xmlDoc3.DocumentElement.RemoveAttribute("xsi");

            Assert.IsTrue(xmlDoc.InnerXml == xmlDoc3.InnerXml);
        }

        //TC.DF13
        private static string[] CreateStateMachineDescriptor_UndeserializableContent =
        {
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingPath.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingPaths.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingProperties.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingState.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingTransition.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingPropertiesAttribute.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingStateAttribute.xml",
             $@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingTransitionAttribute.xml",
        };
        [TestCaseSource(nameof(CreateStateMachineDescriptor_UndeserializableContent))]
        public void CreateStateMachineDescriptor_WithUndeserializableContent_ThrowsException(string path)
        {
            var content = System.IO.File.ReadAllText(path);
            Assert.Throws<InvalidOperationException>(() => factory.CreateStateMachineDescriptor(content));
        }

        //TC.DF14
        [TestCaseSource(nameof(CreateStateMachineDescriptor_DeserializableContent))]
        public void CreateStateMachineDescriptor_WithSerializableInstance_ReturnsSameContentAfterSerialization(string path)
        {
            string expected = System.IO.File.ReadAllText(path);
            var xmlDoc = new XmlDocument();
            xmlDoc.LoadXml(expected);
            xmlDoc.DocumentElement!.RemoveAttribute("xmlns:xsi");
            xmlDoc.DocumentElement.RemoveAttribute("xmlns:xsd");
            xmlDoc.DocumentElement.RemoveAttribute("xsi:noNamespaceSchemaLocation");
            IStateMachineDescriptor? descriptor = factory.CreateStateMachineDescriptor(expected);

            string current = factory.CreateStateMachineDescriptor(descriptor);
            var xmlDoc3 = new XmlDocument();
            xmlDoc3.LoadXml(current);
            xmlDoc3.DocumentElement!.RemoveAttribute("xmlns:xsi");
            xmlDoc3.DocumentElement.RemoveAttribute("xmlns:xsd");
            xmlDoc3.DocumentElement.RemoveAttribute("xsi");

            Assert.IsTrue(xmlDoc.InnerXml == xmlDoc3.InnerXml);
        }

        //TC.DF15
        private static IStateMachineDescriptor?[] CreateStateMachineDescriptor_UnserializableInstance =
        {
             null,
             new StateMachineDescriptor(),
         };
        [TestCaseSource(nameof(CreateStateMachineDescriptor_UnserializableInstance))]
        public void CreateStateMachineDescriptor_WithUnserializableInstance_ThrowsException(IStateMachineDescriptor descriptor)
        {
            Assert.Throws<InvalidOperationException>(() => factory.CreateStateMachineDescriptor(descriptor));
        }
    }
}

using BaSyx.Utils.Settings.Types;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.API.Components;
using BaSyx.API.AssetAdministrationShell.Extensions;
using BaSyx.AAS.Server.Http;

using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors.Contracts;
using TestDemonstrator.TestObjects;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.Enums;

namespace TestDemonstrator.Tests.TestObjectTests
{
    public class TestObjectTests
    {
        private IDescriptorFactory? factory = null;
        private ITicketBuilder? builder = null;
        private static string root = @"../../../DescriptorFactoryTests/TestData";

        [SetUp]
        public void Setup()
        {
            factory = new DescriptorFactory();
            builder = new TicketBuilder();
        }
        
        //TC.TO01
        private static string[] InstantiateTestObject_FromValidPassiveTicket_Data =
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
        [TestCaseSource(nameof(InstantiateTestObject_FromValidPassiveTicket_Data))]
        public void InstantiateTestObject_FromValidPassiveTicket_ReturnsValidHandler(string path)
        {
            string aasContent = System.IO.File.ReadAllText(path);
            string structDescrContent = System.IO.File.ReadAllText(path);
            IAssetAdministrationShell aas = factory!.CreatePassiveShell(aasContent);
            IStructureDescriptor structureDescriptor = factory!.CreateStructureDescriptor(structDescrContent);
            ITestTicket ticket = builder!
                .WithType(TicketType.Passive)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(aas) 
                .Build();

            ITestObject testObject = new TestObject(ticket);

            Assert.IsNotNull(testObject);
        }

        //TC.TO02
        private static TestCaseData[] InstantiateTestObject_FromValidActiveTicket_Data =
        {
               new TestCaseData(
                   $@"http://localhost:4999",
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(InstantiateTestObject_FromValidActiveTicket_Data))]
        public void InstantiateTestObject_FromValidActiveTicket_ReturnsValidHandler(string uriString, string structureDescriptorPath, string stateMachinepath)
        {
            //Creating the real test object
            ServerSettings aasServerSettings = ServerSettings.CreateSettings();
            aasServerSettings.ServerConfig.Hosting.Urls.Add($"{uriString}");
            Identifier identifier = new Identifier("Test", KeyType.Custom);
            IAssetAdministrationShellServiceProvider serviceProvider = new AssetAdministrationShell("Test", identifier).CreateServiceProvider(true);
            serviceProvider.UseAutoEndpointRegistration(aasServerSettings.ServerConfig);
            AssetAdministrationShellHttpServer aasServer = new AssetAdministrationShellHttpServer(aasServerSettings);
            aasServer.SetServiceProvider(serviceProvider);
            aasServer.RunAsync();

            //Building the corresponding ticket
            string structDescrContent = System.IO.File.ReadAllText(structureDescriptorPath);
            string stateMachineDescrContent = System.IO.File.ReadAllText(stateMachinepath);
            IStructureDescriptor structureDescriptor = factory!.CreateStructureDescriptor(structDescrContent);
            IStateMachineDescriptor stateMachineDescriptor = factory!.CreateStateMachineDescriptor(stateMachineDescrContent);
            ITestTicket ticket = builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uriString))
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            //Contextualizing the test object
            ITestObject testObject = new TestObject(ticket);

            //Asserting it was build successfully
            Assert.IsNotNull(testObject);
        }

        //TC.TO03
        private static TestCaseData[] InstantiateTestObject_FromActiveTicketWithNonExistentUri_Data =
        {
               new TestCaseData(
                   $@"http://localhost:65535",
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(InstantiateTestObject_FromActiveTicketWithNonExistentUri_Data))]
        public void InstantiateTestObject_FromActiveTicketWithNonExistentAAS_ThrowsException(string uriString, string structureDescriptorPath, string stateMachinepath)
        {
            string structDescrContent = System.IO.File.ReadAllText(structureDescriptorPath);
            string stateMachineDescrContent = System.IO.File.ReadAllText(stateMachinepath);
            IStructureDescriptor structureDescriptor = factory!.CreateStructureDescriptor(structDescrContent);
            IStateMachineDescriptor stateMachineDescriptor = factory!.CreateStateMachineDescriptor(stateMachineDescrContent);
            ITestTicket ticket = builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uriString))
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            Assert.Throws<ArgumentException>(() => new TestObject(ticket));
        }
    }
}


using TestDemonstrator.Enums;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;


namespace TestDemonstrator.Tests.TicketBuilderTests
{
    public class BuilderTests
    {
        private ITicketBuilder? builder = null;
        private IDescriptorFactory? factory = null;
        private static string root = @"../../../DescriptorFactoryTests/TestData";

        [SetUp]
        public void Setup()
        {
            builder = new TicketBuilder();
            factory = new DescriptorFactory();
        }

        //TC.TB01
        private static string[] BuildPassiveTicket_Data =
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
        [TestCaseSource(nameof(BuildPassiveTicket_Data))]
        public void BuildPassiveTicket_ReturnsValidPassiveTicket(string path)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(path));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(path));

            ITestTicket ticket = builder!
                .WithType(TicketType.Passive)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build();

            Assert.IsNotNull(ticket);
            Assert.IsNotNull(ticket.Type);
            Assert.IsNotNull(ticket.StructureDescriptor);
            Assert.IsNotNull(ticket.PassiveShell);
        }

        //TC.TB02
        private static TestCaseData[] BuildActiveTicket_Data =
        {
               new TestCaseData(
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml",
                   $@"http://localhost:4999")
        };
        [TestCaseSource(nameof(BuildActiveTicket_Data))]
        public void BuildActiveTicket_WithStateMachineDescriptor_ReturnsValidActiveTicket(string structurePath, string statesPath, string uri)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            var ticket = builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uri))
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            Assert.IsNotNull(ticket);
            Assert.That(ticket.Type, Is.EqualTo(TicketType.Active));
            Assert.That(ticket.Uri!.ToString(), Is.EqualTo(uri + "/"));
            Assert.IsNotNull(ticket.StructureDescriptor);
            Assert.IsNotNull(ticket.StateMachineDescriptor);
        }

        //TC.TB02
        private static TestCaseData[] BuildActiveTicket_WithoutStateMachineDescriptor =
        {
               new TestCaseData(
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"http://localhost:4999")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithoutStateMachineDescriptor))]
        public void BuildActiveTicket_WithoutStateMachineDescriptor_ReturnsValidActiveTicket(string shellPath, string uri)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(shellPath));

            var ticket = builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uri))
                .WithStructureDescriptor(structureDescriptor)
                //.WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            Assert.IsNotNull(ticket);
            Assert.That(ticket.Type, Is.EqualTo(TicketType.Active));
            Assert.That(ticket.Uri!.ToString(), Is.EqualTo(uri + "/"));
            Assert.IsNotNull(ticket.StructureDescriptor);
            Assert.IsNull(ticket.StateMachineDescriptor);
        }

        //TC.TB03
        private static TestCaseData[] BuildPassiveTicket_WithActiveType_Data =
        {
            new TestCaseData(
                    TicketType.Active,
                    $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithActiveType_Data))]
        public void BuildPassiveTicket_WithActiveType_ThrowsException(TicketType type, string path)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(path));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(path));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(type)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build());
        }

        //TC.TB04
        private static TestCaseData[] BuildPassiveTicket_WithUndefinedType_Data =
        {
            new TestCaseData(
                    TicketType.Undefined,
                    $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithUndefinedType_Data))]
        public void BuildPassiveTicket_WithUndefinedType_ThrowsException(TicketType type, string path)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(path));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(path));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(type)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build());
        }

        //TC.TB05
        private static TestCaseData[] BuildPassiveTicket_WithoutStructureDescriptor =
        {
               new TestCaseData($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithoutStructureDescriptor))]
        public void BuildPassiveTicket_WithoutStructureDescriptor_ThrowsException(string path)
        {
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(path));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                //.WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build());
        }

        //TC.TB06
        private static TestCaseData[] BuildPassiveTicket_WithoutPassiveShell =
        {
               new TestCaseData($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithoutPassiveShell))]
        public void BuildPassiveTicket_WithoutPassiveShell_ThrowsException(string path)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(path));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithStructureDescriptor(structureDescriptor)
                //.WithPassiveAdministrationShell(passiveShell)
                .Build());
        }

        //TC.TB07
        private static TestCaseData[] BuildPassiveTicket_WithStateMachineDescriptor =
        {
               new TestCaseData($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml", 
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithStateMachineDescriptor))]
        public void BuildPassiveTicket_WithStateMachineDescriptor_ThrowsException(string structurePath, string statesPath)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(structurePath));
            var statesDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .WithStateMachineDescriptor(statesDescriptor)
                .Build());
        }

        //TC.TB08
        private static TestCaseData[] BuildPassiveTicket_WithUri =
        {
               new TestCaseData(
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"http://localhost:4999")
        };
        [TestCaseSource(nameof(BuildPassiveTicket_WithUri))]
        public void BuildPassiveTicket_WithUri_ThrowsException(string structurePath, string uri)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(structurePath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .WithUrl(new Uri(uri))
                .Build());
        }

        //TC.TB09
        private static TestCaseData[] BuildActiveTicket_WithUndefinedType_Data =
        {
               new TestCaseData(
                   TicketType.Undefined,
                   $@"http://localhost:4999",
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithUndefinedType_Data))]
        public void BuildActiveTicket_WithUndefinedType_ThrowsException(TicketType type, string uri, string structurePath, string statesPath)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(type)
                .WithUrl(new Uri(uri))
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build());
        }

        //TC.TB10
        private static TestCaseData[] BuildActiveTicket_WithPassiveType_Data =
        {
               new TestCaseData(
                   TicketType.Passive,
                   $@"http://localhost:4999",
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithPassiveType_Data))]
        public void BuildActiveTicket_WithPassiveType_ThrowsException(TicketType type, string uri, string structurePath, string statesPath)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(type)
                .WithUrl(new Uri(uri))
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build());
        }

        //TC.TB11
        private static TestCaseData[] BuildActiveTicket_WithoutUri =
        {
               new TestCaseData(
                   $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                   $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithoutUri))]
        public void BuildActiveTicket_WithoutUri_ThrowsException(string structurePath, string statesPath)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build());
        }

        //TC.TB12       
        private static TestCaseData[] BuildActiveTicket_WithoutStructureDescriptor =
        {
               new TestCaseData($@"http://localhost:4999", $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithoutStructureDescriptor))]
        public void BuildActiveTicket_WithoutStructureDescriptor_ThrowsException(string uri, string statesPath)
        {
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uri))
                //.WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build());
        }

        //TC.TB13       
        private static TestCaseData[] BuildActiveTicket_WithPassiveShell =
        {
            new TestCaseData(
                $@"http://localhost:4999",
                $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml",
                $@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml")
        };
        [TestCaseSource(nameof(BuildActiveTicket_WithPassiveShell))]
        public void BuildActiveTicket_WithPassiveShell_ThrowsException(string uri, string structurePath, string statesPath)
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(structurePath));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(structurePath));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText(statesPath));

            Assert.Throws<InvalidOperationException>(() => builder!
                .WithType(TicketType.Active)
                .WithUrl(new Uri(uri))
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build());
        }
    }
}

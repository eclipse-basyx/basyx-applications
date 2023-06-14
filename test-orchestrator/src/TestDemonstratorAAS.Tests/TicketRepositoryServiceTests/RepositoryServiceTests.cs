using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;
using TestDemonstratorAAS.RepositoryService;

namespace TestDemonstratorAAS.Tests.RepositoryServiceTests
{
    public class RepositoryServiceTests
    {
        private TicketRepositoryService repositoryService = null!;
        private TicketDbRepository ticketRepository = null!;
        private TicketBuilder ticketBuilder = null!;
        private DescriptorFactory descriptorFactory = null!;
        private static string root = $"../../../TicketRepositoryServiceTests/TestData";

        [OneTimeSetUp]
        public void OneTimeSetup()
        {
            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                .AddJsonFile("appsettings.Development.json")
                .Build();

            DbContextOptions<TestTicketContext> options = new DbContextOptionsBuilder<TestTicketContext>()
                .UseSqlServer(configuration.GetConnectionString("SqlServerConnection"))
                .Options;

            ticketRepository = new TicketDbRepository(options);
            ticketBuilder = new TicketBuilder();
            descriptorFactory = new DescriptorFactory();
            
            repositoryService = new TicketRepositoryService(ticketRepository, ticketBuilder, descriptorFactory);
        }

        [SetUp]
        public void Setup()
        {
            ticketRepository.dbContext!.Database.EnsureDeleted();
            ticketRepository.dbContext!.Database.EnsureCreated();
        }

        //TC.TRS01
        private static TestCaseData[] UploadActiveTicket_InvalidArguments =
        {
             new TestCaseData(
                 string.Empty,
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $"Invalid Uniform Resource Identifier (URI) content detected!"),

             new TestCaseData(
                 "http://localhost:4999",
                 string.Empty,
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $"Invalid structure descriptor content detected!"),

             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 string.Empty,
                 $"Invalid state machine descriptor content detected!"),
        };
        [TestCaseSource(nameof(UploadActiveTicket_InvalidArguments))]
        public void UploadActiveTicket_WithNullOrEmptyArgument_ThrowsException(string uriString, string structureContent, string statesContent, string message)
        {
            var ex = Assert.Throws<ArgumentException>(
                        () => repositoryService.UploadActiveTicket(uriString, structureContent, statesContent));
            Assert.That(ex.Message, Is.EqualTo(message));
        }

        //TC.TRS02
        private static TestCaseData[] UploadActiveTicket_InvalidUri =
        {
             new TestCaseData(
                 "http://localhost:65536",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
             new TestCaseData(
                 "http:/invalid:65533",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UploadActiveTicket_InvalidUri))]
        public void UploadActiveTicket_WithInvalidUri_ThrowsException(string uriString, string structureContent, string statesContent)
        {
            Assert.Throws<UriFormatException>(() => repositoryService.UploadActiveTicket(uriString, structureContent, statesContent));
        }

        //TC.TRS03
        private static TestCaseData[] UploadActiveTicket_InvalidStructureDescriptor =
        {
             new TestCaseData(
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UploadActiveTicket_InvalidStructureDescriptor))]
        public void UploadActiveTicket_WithInvalidStructureDescriptor_ThrowsException(string uriString, string structureContent, string statesContent)
        {
            Assert.That(() => repositoryService.UploadActiveTicket(uriString, structureContent, statesContent), Throws.Exception);
        }

        //TC.TRS04
        private static TestCaseData[] UploadActiveTicket_InvalidStateMachineDescriptor =
        {
             new TestCaseData(
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingPath.xml")),
        };
        [TestCaseSource(nameof(UploadActiveTicket_InvalidStateMachineDescriptor))]
        public void UploadActiveTicket_InvalidStateMachineDescriptor_ThrowsException(string uriString, string structureContent, string statesContent)
        {
            Assert.That(() => repositoryService.UploadActiveTicket(uriString, structureContent, statesContent), Throws.Exception);
        }

        //TC.TRS05
        private static TestCaseData[] UploadActiveTicket_ValidArguments =
        {
             new TestCaseData(
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UploadActiveTicket_ValidArguments))]
        public void UploadActiveTicket_WithValidArguments_UploadsTicket(string uriString, string structureContent, string statesContent)
        {
            string ret = repositoryService.UploadActiveTicket(uriString, structureContent, statesContent);
            Assert.IsTrue(ret.Contains($"Successfully uploaded ticket with Id:"));
            Assert.That(ticketRepository.Tickets.Count, Is.EqualTo(1));
        }


        //TC.TRS06
        private static TestCaseData[] UploadPassiveTicket_InvalidPassiveShell =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(UploadPassiveTicket_InvalidPassiveShell))]
        public void UploadPassiveTicket_InvalidPassiveShell_ThrowsException(string passiveShellContent, string structureContent)
        {
            Assert.That(() => repositoryService.UploadPassiveTicket(passiveShellContent, structureContent), Throws.Exception);
        }

        //TC.TRS07
        private static TestCaseData[] UploadPassiveTicket_InvalidStructureDescriptor =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"))
        };
        [TestCaseSource(nameof(UploadPassiveTicket_InvalidStructureDescriptor))]
        public void UploadPassiveTicket_InvalidStructureContent_ThrowsException(string passiveShellContent, string structureContent)
        {
            Assert.That(() => repositoryService.UploadPassiveTicket(passiveShellContent, structureContent), Throws.Exception);
        }

        //TC.TRS08
        private static TestCaseData[] UploadPassiveTicket_ValidArguments =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(UploadPassiveTicket_ValidArguments))]
        public void UploadPassiveTicket_WithValidArguments_UploadsTicket(string passiveShellContent, string structureContent)
        {
            string ret = repositoryService.UploadPassiveTicket(passiveShellContent, structureContent);
            Assert.IsTrue(ret.Contains($"Successfully uploaded ticket with Id:"));
            Assert.That(ticketRepository.Tickets.Count, Is.EqualTo(1));
        }


        //TC.TRS09
        private static TestCaseData[] UpdateActiveTicket_InvalidArguments =
        {
            new TestCaseData(
                 string.Empty,
                "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $"Invalid ticket ID detected!"),

             new TestCaseData(
                 new Guid().ToString(),
                 string.Empty,
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $"Invalid Uniform Resource Identifier (URI) content detected!"),

             new TestCaseData(
                 new Guid().ToString(),
                 "http://localhost:4999",
                 string.Empty,
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml"),
                 $"Invalid structure descriptor content detected!"),

             new TestCaseData(
                 new Guid().ToString(),
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 string.Empty,
                 $"Invalid state machine descriptor content detected!"),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_InvalidArguments))]
        public void UpdateActiveTicket_WithNullOrEmptyArgument_ThrowsException(string guid, string uriString, string structureContent, string statesContent, string message)
        {
            var ex = Assert.Throws<ArgumentException>(
                        () => repositoryService.UpdateActiveTicket(guid, uriString, structureContent, statesContent));
            Assert.That(ex.Message, Is.EqualTo(message));
        }

        //TC.TRS10
        private static TestCaseData[] UpdateActiveTicket_InvalidId =
        {
             new TestCaseData(
                 "00000000-0000-0000-0000-000000000000 0",
                 "http://localhost:65536",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_InvalidId))]
        public void UpdateActiveTicket_WithInvalidId_ThrowsException(string guid, string uriString, string structureContent, string statesContent)
        {
            Assert.Throws<FormatException>(() => repositoryService.UpdateActiveTicket(guid, uriString, structureContent, statesContent));
        }

        //TC.TRS11
        private static TestCaseData[] UpdateActiveTicket_InvalidUri =
        {
             new TestCaseData(
                 new Guid().ToString(),
                 "http://localhost:65536",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
             new TestCaseData(
                 new Guid().ToString(),
                 "http:/invalid:65533",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_InvalidUri))]
        public void UpdateActiveTicket_WithInvalidUri_ThrowsException(string guid, string uriString, string structureContent, string statesContent)
        {
            Assert.Throws<UriFormatException>(() => repositoryService.UpdateActiveTicket(guid, uriString, structureContent, statesContent));
        }

        //TC.TRS12
        private static TestCaseData[] UpdateActiveTicket_InvalidStructureDescriptor =
        {
             new TestCaseData(
                 new Guid().ToString(),
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_InvalidStructureDescriptor))]
        public void UpdateActiveTicket_WithInvalidStructureDescriptor_ThrowsException(string guid, string uriString, string structureContent, string statesContent)
        {
            Assert.That(() => repositoryService.UpdateActiveTicket(guid, uriString, structureContent, statesContent), Throws.Exception);
        }

        //TC.TRS13
        private static TestCaseData[] UpdateActiveTicket_InvalidStateMachineDescriptor =
        {
             new TestCaseData(
                 new Guid().ToString(),
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD_MissingPath.xml")),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_InvalidStateMachineDescriptor))]
        public void UpdateActiveTicket_InvalidStateMachineDescriptor_ThrowsException(string guid, string uriString, string structureContent, string statesContent)
        {
            Assert.That(() => repositoryService.UpdateActiveTicket(guid, uriString, structureContent, statesContent), Throws.Exception);
        }

        //TC.TRS14
        private static TestCaseData[] UpdateActiveTicket_ValidArguments =
        {
            new TestCaseData(
                "http://localhost:4999",
                File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(UpdateActiveTicket_ValidArguments))]
        public void UpdateActiveTicket_WithValidArguments_UpdatesTicket(string uriString, string structureContent, string statesContent)
        {
            repositoryService.UploadActiveTicket(uriString, structureContent, statesContent);

            var id = repositoryService
                        .TicketRepository
                        .Tickets
                        .Single()
                        .Id
                        .ToString();

            string ret = repositoryService.UpdateActiveTicket(id, uriString, structureContent, statesContent);
            Assert.IsTrue(ret.Contains($"Successfully updated ticket with Id:"));
            Assert.That(ticketRepository.Tickets.Count, Is.EqualTo(1));
            Assert.IsNotNull(ticketRepository.Tickets.Single().UpdatedAt);
        }


        //TC.TRS15
        private static TestCaseData[] UpdatePassiveTicket_InvalidPassiveShell =
        {
             new TestCaseData(
                 new Guid().ToString(),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(UpdatePassiveTicket_InvalidPassiveShell))]
        public void UpdatePassiveTicket_InvalidPassiveShell_ThrowsException(string guid, string passiveShellContent, string structureContent)
        {
            Assert.That(() => repositoryService.UpdatePassiveTicket(guid, passiveShellContent, structureContent), Throws.Exception);
        }

        //TC.TRS16
        private static TestCaseData[] UpdatePassiveTicket_InvalidStructureDescriptor =
        {
             new TestCaseData(
                 new Guid().ToString(),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/04_Sorting_AAS.xml"))
        };
        [TestCaseSource(nameof(UpdatePassiveTicket_InvalidStructureDescriptor))]
        public void UpdatePassiveTicket_InvalidStructureContent_ThrowsException(string guid, string passiveShellContent, string structureContent)
        {
            Assert.That(() => repositoryService.UpdatePassiveTicket(guid, passiveShellContent, structureContent), Throws.Exception);
        }

        //TC.TRS17
        private static TestCaseData[] UpdatePassiveTicket_ValidArguments =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(UpdatePassiveTicket_ValidArguments))]
        public void UpdatePassiveTicket_WithValidArguments_UploadsTicket(string passiveShellContent, string structureContent)
        {
            repositoryService.UploadPassiveTicket(passiveShellContent, structureContent);
            
            var id = repositoryService
                        .TicketRepository
                        .Tickets
                        .Single()
                        .Id
                        .ToString();

            string ret = repositoryService.UpdatePassiveTicket(id, passiveShellContent, structureContent);
            Assert.IsTrue(ret.Contains($"Successfully updated ticket with Id:"));
            Assert.That(ticketRepository.Tickets.Count, Is.EqualTo(1));
            Assert.IsNotNull(ticketRepository.Tickets.Single().UpdatedAt);
        }


        //TC.TRS18
        [Test]
        [TestCase("")]
        public void DeleteTicket_WithInvalidId_ThrowsException(string guid)
        {
            Assert.Throws<ArgumentException>(() => repositoryService.DeleteTicket(guid));
        }

        //TC.TRS19
        [Test]
        public void DeleteTicket_WithUnknownId_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => repositoryService.DeleteTicket(new Guid().ToString()));
        }

        //TC.TRS20
        private static TestCaseData[] DeleteTicket_ValidArguments =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(DeleteTicket_ValidArguments))]
        public void DeleteTicket_DeletesTicket(string passiveShellContent, string structureContent)
        {
            repositoryService.UploadPassiveTicket(passiveShellContent, structureContent);

            var id = repositoryService
                        .TicketRepository
                        .Tickets
                        .Single()
                        .Id
                        .ToString();

            string ret = repositoryService.DeleteTicket(id);
            Assert.That(ticketRepository.Tickets.Count, Is.EqualTo(0));
        }



        //TC.TRS21
        [Test]
        [TestCase("")]
        public void RetrieveTicketById_WithInvalidId_ThrowsException(string guid)
        {
            ITestTicket? ticket = null;
            Assert.Throws<ArgumentException>(() => repositoryService.RetrieveTicketById(guid, ref ticket));
        }

        //TC.TRS22
        [Test]
        public void RetrieveTicketById_WithUnknownId_ThrowsException()
        {
            ITestTicket? ticket = null;
            Assert.Throws<ArgumentException>(() => repositoryService.RetrieveTicketById(new Guid().ToString(), ref ticket));
        }

        //TC.TRS23
        private static TestCaseData[] RetrieveTicketById_ValidArguments =
        {
             new TestCaseData(
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml")),
        };
        [TestCaseSource(nameof(RetrieveTicketById_ValidArguments))]
        public void RetrieveTicketById_RetrievesTicket(string passiveShellContent, string structureContent)
        {
            repositoryService.UploadPassiveTicket(passiveShellContent, structureContent);

            var id = repositoryService
                        .TicketRepository
                        .Tickets
                        .Single()
                        .Id
                        .ToString();

            ITestTicket? ticket = null;

            string ret = repositoryService.RetrieveTicketById(id, ref ticket);
            Assert.IsNotNull(ticket);
            Assert.IsTrue(ret.Contains($"Successfully obtained ticket with Id: {ticket.Id}"));
        }



        //TC.TRS24
        [Test]
        [TestCase("")]
        public void RetrieveTicketByUri_WithInvalidUri_ThrowsException(string uri)
        {
            ITestTicket? ticket = null;
            Assert.Throws<ArgumentException>(() => repositoryService.RetrieveTicketByUri(uri, ref ticket));
        }

        //TC.TRS25
        [Test]
        [TestCase("http://localhost:4999")]
        public void RetrieveTicketByUri_WithUnknownUri_ThrowsException(string uri)
        {
            ITestTicket? ticket = null;
            Assert.Throws<ArgumentException>(() => repositoryService.RetrieveTicketByUri(uri, ref ticket));
        }

        //TC.TRS26
        private static TestCaseData[] RetrieveTicketByUri_ValidArguments =
        {
             new TestCaseData(
                 "http://localhost:4999",
                 File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"),
                 File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD.xml")),
        };
        [TestCaseSource(nameof(RetrieveTicketByUri_ValidArguments))]
        public void RetrieveTicketByUri_RetrievesTicket(string uri, string structureContent, string statesContent)
        {
            repositoryService.UploadActiveTicket(uri, structureContent, statesContent);

            var id = repositoryService
                        .TicketRepository
                        .Tickets
                        .Single()
                        .Id
                        .ToString();

            ITestTicket? ticket = null;

            string ret = repositoryService.RetrieveTicketById(id, ref ticket);
            Assert.IsNotNull(ticket);
            Assert.IsTrue(ret.Contains($"Successfully obtained ticket with Id: {ticket.Id}"));
        }
    }
}
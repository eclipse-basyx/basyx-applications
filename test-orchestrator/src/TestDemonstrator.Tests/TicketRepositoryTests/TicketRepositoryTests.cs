using AutoMapper;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System.Xml;

using TestDemonstrator.Enums;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;

namespace TestDemonstrator.Tests.TicketRepositoryTests
{
    public class TicketRepositoryTests
    {
        private ITicketRepository? repository = null;
        private ITicketBuilder? builder = null;
        private IDescriptorFactory? factory = null;

        private static string root = @"../../../DescriptorFactoryTests/TestData";

        private ITestTicket? activeTicket = null;
        private ITestTicket? passiveTicket = null;

        [OneTimeSetUp]
        public void OneTimeSetup()
        {
            factory = new DescriptorFactory();
            builder = new TicketBuilder();
        }

        [SetUp]
        public void Setup() 
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml"));
            var uri = new Uri($@"http://localhost:4999");

            activeTicket = builder!
                .WithType(TicketType.Active)
                .WithUrl(uri)
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            passiveTicket = builder!
                .WithType(TicketType.Passive)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build();

            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                .AddJsonFile("appsettings.Development.json")
                .Build();

            DbContextOptions<TestTicketContext> options = new DbContextOptionsBuilder<TestTicketContext>()
                .UseSqlServer(configuration.GetConnectionString("SqlServerConnection"))
                .Options;

            repository = new TicketDbRepository(options);

            (repository as TicketDbRepository)!.dbContext!.Database.EnsureDeleted();
            (repository as TicketDbRepository)!.dbContext!.Database.EnsureCreated();
        }

        //TC.RE01
        [Test]
        public void UploadTicket_UploadsPassiveTicketSuccessfully()
        {
            repository!.Upload(passiveTicket!);
            var context = (repository as TicketDbRepository)!.dbContext!;

            var xmlDocExpected = new XmlDocument();
            var xmlDocActual = new XmlDocument();

            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            xmlDocActual.LoadXml(context!.Tickets.First()!.PassiveShell!);
            
            var expectedShell = xmlDocExpected.ChildNodes[1]!.InnerXml;
            var actualShell = xmlDocActual.ChildNodes[0]!.InnerXml;

            xmlDocActual.LoadXml(context!.Tickets.First()!.StructureDescriptor!);

            var expectedStructure = xmlDocExpected.ChildNodes[1]!.InnerXml;
            var actualStructure = xmlDocActual.ChildNodes[0]!.InnerXml;

            Assert.That(context!.Tickets.Count(), Is.EqualTo(1));
            Assert.IsNotNull(context!.Tickets.First().TicketId);
            Assert.IsNotNull(context!.Tickets.First().CreatedAt);
            Assert.IsNull(context!.Tickets.First().UpdatedAt);
            Assert.That(context!.Tickets.First().Type, Is.EqualTo("Passive"));
            Assert.IsNull(context!.Tickets.First().Uri);
            Assert.That(actualShell, Is.EqualTo(expectedShell));
            Assert.That(actualStructure, Is.EqualTo(expectedStructure));
            Assert.IsNull(context!.Tickets.First().StateMachineDescriptor);
            Assert.IsNull(context!.Tickets.First().TestReport);
        }

        //TC.RE02
        [Test]
        public void UploadTicket_UploadsActiveTicketSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;
            repository!.Upload(activeTicket!);

            var xmlDocExpected = new XmlDocument();
            var xmlDocActual = new XmlDocument();
            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            xmlDocActual.LoadXml(context!.Tickets.First()!.StructureDescriptor!);
            var expectedStructure = xmlDocExpected.ChildNodes[1]!.InnerXml;
            var actualStructure = xmlDocActual.ChildNodes[0]!.InnerXml;

            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml"));
            xmlDocActual.LoadXml(context!.Tickets.First()!.StateMachineDescriptor!);
            var expectedStateMachine = xmlDocExpected.InnerXml;
            var actualStateMachine = xmlDocExpected.InnerXml;

            Assert.That(context!.Tickets.Count(), Is.EqualTo(1));
            Assert.IsNotNull(context!.Tickets.First().TicketId);
            Assert.IsNotNull(context!.Tickets.First().CreatedAt);
            Assert.IsNull(context!.Tickets.First().UpdatedAt);
            Assert.That(context!.Tickets.First().Type, Is.EqualTo("Active"));
            Assert.That(context!.Tickets.First().Uri, Is.EqualTo($@"http://localhost:4999/"));
            Assert.IsNull(context!.Tickets.First()!.PassiveShell!);
            Assert.That(actualStructure, Is.EqualTo(expectedStructure));
            Assert.That(actualStateMachine, Is.EqualTo(expectedStateMachine));
            Assert.IsNull(context!.Tickets.First().TestReport);
        }

        //TC.RE03
        [Test]
        public void DeleteTicket_DeletesUploadedTicketSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;

            repository!.Upload(passiveTicket!);
            repository!.Delete(passiveTicket!);
            Assert.That(context.Tickets.Count(), Is.EqualTo(0));

            repository!.Upload(activeTicket!);
            repository!.Delete(activeTicket!);
            Assert.That(context.Tickets.Count(), Is.EqualTo(0));
        }

        //TC.RE04
        [Test]
        public void UpdateTicket_UpdatesUploadedTicketSuccessfully() 
        {
            var context = (repository as TicketDbRepository)!.dbContext!;

            repository!.Upload(activeTicket!);

            activeTicket!.TestReport = "Test";
            repository!.Update(activeTicket!);

            Assert.IsNotNull(context.Tickets.First().UpdatedAt);
            Assert.That(context.Tickets.First().TestReport, Is.EqualTo("Test"));
        }

        //TC.RE05
        [Test]
        public void UpdateTicket_OnUnknownTicket_ThrowsException()
        {
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            var stateMachineDescriptor = factory!.CreateStateMachineDescriptor(File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml"));
            var uri = new Uri($@"http://localhost:4999");

            var unknownTicket = builder!
                .WithType(TicketType.Active)
                .WithUrl(uri)
                .WithStructureDescriptor(structureDescriptor)
                .WithStateMachineDescriptor(stateMachineDescriptor)
                .Build();

            Assert.Throws<ArgumentException>(() => repository!.Update(unknownTicket));
        }

        //TC.RE06
        [Test]
        public void DeleteTicketById_DeletesUploadedTicketSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;

            repository!.Upload(passiveTicket!);
            repository!.DeleteTicketById(passiveTicket!.Id);
            Assert.That(context.Tickets.Count(), Is.EqualTo(0));
        }

        //TC.RE07
        [Test]
        public void DeleteTicketById_OnUnknownTicket_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => repository!.DeleteTicketById(new Guid()));
        }

        //TC.RE08
        [Test]
        public void RetrieveTicketById_RetrievesUploadedTicketSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;

            repository!.Upload(activeTicket!);
            var ticket = repository!.RetrieveTicketById(activeTicket!.Id);

            var xmlDocExpected = new XmlDocument();
            var xmlDocActual = new XmlDocument();
            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            xmlDocActual.LoadXml((repository as TicketDbRepository)!.dbContext!.Tickets.First()!.StructureDescriptor!);
            var expectedStructure = xmlDocExpected.ChildNodes[1]!.InnerXml;
            var actualStructure = xmlDocActual.ChildNodes[0]!.InnerXml;

            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml"));
            xmlDocActual.LoadXml(context.Tickets.First()!.StateMachineDescriptor!);
            var expectedStateMachine = xmlDocExpected.InnerXml;
            var actualStateMachine = xmlDocExpected.InnerXml;

            Assert.That(ticket!.Id, Is.EqualTo(activeTicket!.Id));
            Assert.That(ticket!.CreatedAt, Is.EqualTo(activeTicket!.CreatedAt));
            Assert.That(ticket!.UpdatedAt, Is.EqualTo(activeTicket!.UpdatedAt));
            Assert.That(ticket!.Type, Is.EqualTo(activeTicket!.Type));
            Assert.That(ticket!.Uri, Is.EqualTo(activeTicket!.Uri));
            Assert.That(ticket!.PassiveShell!, Is.EqualTo(activeTicket!.PassiveShell));
            Assert.That(actualStructure, Is.EqualTo(expectedStructure));
            Assert.That(actualStateMachine, Is.EqualTo(expectedStateMachine));
            Assert.That(ticket.TestReport, Is.EqualTo(activeTicket!.TestReport));
        }

        //TC.RE09
        [Test]
        public void RetrieveTicketById_OnUnknownTicket_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => repository!.RetrieveTicketById(new Guid()));
        }

        //TC.RE10
        [Test]
        public void RetrieveTicketByUri_RetrievesUploadedTicketSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;

            repository!.Upload(activeTicket!);
            var ticket = repository!.RetrieveTicketByUrl(activeTicket!.Uri!.ToString());

            var xmlDocExpected = new XmlDocument();
            var xmlDocActual = new XmlDocument();
            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/PassiveShells/HTW/01_Demo_AAS.xml"));
            xmlDocActual.LoadXml(context.Tickets.First()!.StructureDescriptor!);
            var expectedStructure = xmlDocExpected.ChildNodes[1]!.InnerXml;
            var actualStructure = xmlDocActual.ChildNodes[0]!.InnerXml;

            xmlDocExpected.LoadXml(File.ReadAllText($@"{root}/StateMachineDescriptors/01_PackML_SMD - Copy.xml"));
            xmlDocActual.LoadXml((repository as TicketDbRepository)!.dbContext!.Tickets.First()!.StateMachineDescriptor!);
            var expectedStateMachine = xmlDocExpected.InnerXml;
            var actualStateMachine = xmlDocExpected.InnerXml;

            Assert.That(ticket!.Id, Is.EqualTo(activeTicket!.Id));
            Assert.That(ticket!.CreatedAt, Is.EqualTo(activeTicket!.CreatedAt));
            Assert.That(ticket!.UpdatedAt, Is.EqualTo(activeTicket!.UpdatedAt));
            Assert.That(ticket!.Type, Is.EqualTo(activeTicket!.Type));
            Assert.That(ticket!.Uri, Is.EqualTo(activeTicket!.Uri));
            Assert.That(ticket!.PassiveShell!, Is.EqualTo(activeTicket!.PassiveShell));
            Assert.That(actualStructure, Is.EqualTo(expectedStructure));
            Assert.That(actualStateMachine, Is.EqualTo(expectedStateMachine));
            Assert.That(ticket.TestReport, Is.EqualTo(activeTicket!.TestReport));
        }

        //TC.RE11
        [Test]
        public void RetrieveTicketByUri_OnUnknownTicket_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() => repository!.RetrieveTicketByUrl($@"http://localhost:4999")); 
        }

        //TC.RE12
        [Test]
        public void RetrieveAllTickets_ObtainsAllTicketsSuccessfully()
        {
            var context = (repository as TicketDbRepository)!.dbContext!;
            var mapperConfig = new MapperConfiguration(cfg =>
                cfg.AddProfile<TicketMappingProfile>());
            IMapper mapper = mapperConfig.CreateMapper();

            var uploads = new List<ITestTicket> { passiveTicket!, activeTicket! };
            uploads.ForEach(t => repository!.Upload(t));

            List<ITestTicket> tickets =  repository!.RetrieveAllTickets().ToList();
            var expectedTickets = context.Tickets!
                .ToList()
                .Select(x => mapper.Map<TestTicket>(x))
                .ToArray();

            Assert.That(tickets.Count(), Is.EqualTo(context.Tickets!.Count()));
            for (int i = 0; i < tickets.Count(); i++)
            {
                Assert.That(tickets[i]!.Id, Is.EqualTo(expectedTickets[i].Id));
                Assert.That(tickets[i]!.CreatedAt, Is.EqualTo(expectedTickets[i].CreatedAt));
                Assert.That(tickets[i]!.UpdatedAt, Is.EqualTo(expectedTickets[i].UpdatedAt));
                Assert.That(tickets[i]!.Type, Is.EqualTo(expectedTickets[i].Type));
                Assert.That(tickets[i]!.Uri!, Is.EqualTo(expectedTickets[i]!.Uri));
                Assert.That(tickets[i]!.TestReport, Is.EqualTo(expectedTickets[i]!.TestReport));
            }
        }
    }
}

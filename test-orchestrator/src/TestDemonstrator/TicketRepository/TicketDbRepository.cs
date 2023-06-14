
using AutoMapper;
using Microsoft.EntityFrameworkCore;
using NLog;

using TestDemonstrator.Enums;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TicketRepository.Data;
using TestDemonstrator.TicketRepository.Models;

namespace TestDemonstrator.TicketRepository
{
    public class TicketDbRepository : ITicketRepository
    {
        Logger logger = LogManager.GetCurrentClassLogger();

        private DbContextOptions options;

        private IMapper mapper;

        public TestTicketContext? dbContext;

        public TicketDbRepository(DbContextOptions options)
        {
            var mapperConfig = new MapperConfiguration(cfg =>
                    cfg.AddProfile<TicketMappingProfile>());

            mapper = mapperConfig.CreateMapper();

            this.options = options;

            dbContext = new TestTicketContext(this.options);

            dbContext.Database.EnsureCreated();
        }

        public IEnumerable<ITestTicket> Tickets
        {
            get
            {
                dbContext = new TestTicketContext(this.options);
                
                return dbContext.Tickets
                    .Select(x => mapper.Map<TestTicket>(x))
                    .AsNoTracking()
                    .ToList();
            }
        }

        public IEnumerable<ITestTicket> PassiveTickets
            => RetrieveAllTickets()
                .Where(t => t.Type == TicketType.Passive)
                .ToList();

        public IEnumerable<ITestTicket> ActiveTickets
            => RetrieveAllTickets()
                .Where(t => t.Type == TicketType.Active)
                .ToList();

        public IEnumerable<ITestTicket> ProcessedTickets
            => RetrieveAllTickets()
                .Where(t => t.TestReport != null)
                .ToList();

        public IEnumerable<ITestTicket> UnprocessedTickets
            => RetrieveAllTickets()
                .Where(t => t.TestReport == null)
                .ToList();

        public void Upload(ITestTicket ticket)
        {
            var dbTicket = mapper.Map<TestTicketEntity>(ticket);

            dbContext = new TestTicketContext(this.options);
            dbContext.Tickets.Add(dbTicket);
            dbContext.SaveChanges();
            dbContext.ChangeTracker.Clear();

            logger.Info($"Uploaded {dbTicket.Type.ToLower()} ticket with Id: {ticket.Id}");
        }

        public void Delete(ITestTicket ticket)
        {
            var dbTicket = mapper.Map<TestTicketEntity>(ticket);

            dbContext = new TestTicketContext(this.options);
            dbContext.Tickets.Remove(dbTicket);
            dbContext.SaveChanges();
            dbContext.ChangeTracker.Clear();

            logger.Info($"Deleted {dbTicket.Type.ToLower()} ticket with Id: {ticket.Id}");
        }

        public void Update(ITestTicket ticket)
        {
            var dbTicket = mapper.Map<TestTicketEntity>(ticket);

            dbContext = new TestTicketContext(this.options);
            var oldTicket = dbContext.Tickets
                .Where(t => t.TicketId == dbTicket.TicketId)
                .SingleOrDefault();

            if (oldTicket == null)
            {
                throw new ArgumentException($"There is no ticket with id {dbTicket.TicketId} in the repository! No update possible!");
            }

            oldTicket.Uri = dbTicket.Uri;
            oldTicket.TicketId = dbTicket.TicketId;
            oldTicket.StructureDescriptor = dbTicket.StructureDescriptor;
            oldTicket.StateMachineDescriptor = dbTicket.StateMachineDescriptor;
            oldTicket.PassiveShell = dbTicket.PassiveShell;
            oldTicket.UpdatedAt = DateTime.UtcNow;
            oldTicket.TestReport = dbTicket.TestReport;

            dbContext.SaveChanges();
            dbContext.ChangeTracker.Clear();

            logger.Info($"Updated {dbTicket.Type.ToLower()} ticket with Id: {ticket.Id}");
        }

        public void DeleteTicketById(Guid id)
        {
            dbContext = new TestTicketContext(this.options);
            var ticket = dbContext.Tickets
                .Where(t => t.TicketId == id)
                .SingleOrDefault();

            if (ticket == null)
            {
                throw new ArgumentException($"There is no ticket with id {id} in the repository!");
            }

            dbContext.Tickets.Remove(ticket);
            dbContext.SaveChanges();
            dbContext.ChangeTracker.Clear();

            logger.Info($"Deleted {ticket.Type.ToLower()} ticket with Id: {ticket.TicketId}");
        }

        public ITestTicket RetrieveTicketById(Guid id)
        {
            dbContext = new TestTicketContext(this.options);
            var ticket = dbContext.Tickets
                .Where(t => t.TicketId == id)
                .Select(x => mapper.Map<TestTicket>(x))
                .SingleOrDefault();

            if (ticket == null)
            {
                throw new ArgumentException($"There is no ticket with id {id} in the repository!");
            }

            logger.Info($"Retrieved {ticket.Type.ToString().ToLower()} ticket with Id: {ticket.Id}");
            return ticket;
        }

        public ITestTicket RetrieveTicketByUrl(string uri)
        {
            dbContext = new TestTicketContext(this.options);
            var ticket = dbContext.Tickets
                .Where(t => t.Uri == uri)
                .Select(x => mapper.Map<TestTicket>(x))
                .FirstOrDefault();

            if (ticket == null)
            {
                throw new ArgumentException($"There is no ticket with URI {uri} in the repository!");
            }

            logger.Info($"Retrieved {ticket.Type.ToString().ToLower()} ticket with Uri: {uri}");

            return ticket;
        }

        public IEnumerable<ITestTicket> RetrieveAllTickets()
        {
            dbContext = new TestTicketContext(this.options);
            var tickets = dbContext.Tickets
                .Select(x => mapper.Map<TestTicket>(x))
                .AsNoTracking()
                .ToList();

            logger.Info($"Retrieved all tickets!");
            return tickets;
        }

        public void UploadTestReport(Guid id, string report)
        {
            dbContext = new TestTicketContext(this.options);
            var ticket = dbContext.Tickets
                .Where(t => t.TicketId == id)
                .SingleOrDefault();

            if (ticket == null)
            {
                throw new ArgumentException($"There is no ticket with id {id} in the repository!");
            }

            ticket.TestReport = report;

            dbContext.SaveChanges();
            dbContext.ChangeTracker.Clear();

            logger.Info($"Uploaded test report!");
        }
    }
}

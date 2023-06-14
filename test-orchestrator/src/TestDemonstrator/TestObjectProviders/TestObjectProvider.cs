//Libraries
using NLog;

//Core
using TestDemonstrator.TestObjects;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TestObjectProviders.Contracts;

namespace TestDemonstrator.TestObjectProviders
{
    /// <summary>
    /// The following class serves as a concrete implementation of the <see cref="ITestObjectProvider"/> interface/>
    /// </summary>
    public class TestObjectProvider : ITestObjectProvider
    {
        private static readonly NLog.ILogger logger = LogManager.GetCurrentClassLogger();

        /// <inheritdoc />
        private ITicketRepository ticketRepository;

        /// <summary>
        /// Constructor method
        /// </summary>
        /// <param name="ticketRepository">the ticket repository needed for the construction of the test objects</param>
        public TestObjectProvider(ITicketRepository ticketRepository)
        {
            this.ticketRepository = ticketRepository;
        }

        /// <inheritdoc />
        public ITicketRepository TicketRepository
        {
            get => ticketRepository;
            private set => ticketRepository =
                value ?? throw new NullReferenceException();
        }

        /// <inheritdoc />
        public ITestObject RetrieveTestObject(ITestTicket ticket)
        {
            var testObject = new TestObject(ticket);

            logger.Info($"Successfully created test object for ticket with ID: {ticket.Id}");

            return testObject;
        }

        /// <inheritdoc />
        public IEnumerable<ITestObject> RetrieveTestObjects()
        {
            List<ITestObject> testObjects = new List<ITestObject>();

            foreach (var ticket in TicketRepository.Tickets)
            {
                var testObject = RetrieveTestObject(ticket);

                logger.Info($"Successfully instantiated test object for ticket with ID: {ticket.Id}");

                testObjects.Add(testObject);
            }

            return testObjects;
        }

        /// <inheritdoc />
        public IEnumerable<ITestObject> RetrievePassiveTestObjects()
        {
            List<ITestObject> testObjects = new List<ITestObject>();

            //ticketRepository.PassiveTickets
            //  .Select(t => RetrieveTestObject(t)).ToList();

            foreach (var ticket in TicketRepository.PassiveTickets)
            {
                var testObject = RetrieveTestObject(ticket);

                logger.Info($"Successfully instantiated passive test object for ticket with ID: {ticket.Id}");

                testObjects.Add(testObject);
            }

            return testObjects;
        }

        /// <inheritdoc />
        public IEnumerable<ITestObject> RetrieveActiveTestObjects()
        {
            List<ITestObject> testObjects = new List<ITestObject>();

            foreach (var ticket in TicketRepository.ActiveTickets)
            {
                var testObject = RetrieveTestObject(ticket);

                logger.Info($"Successfully instantiated active test object for ticket with ID: {ticket.Id}");

                testObjects.Add(testObject);
            }

            return testObjects;
        }

        /// <inheritdoc />
        public IEnumerable<ITestObject> RetrieveProcessedTestObjects()
        {
            List<ITestObject> testObjects = new List<ITestObject>();

            foreach (var ticket in TicketRepository.ProcessedTickets)
            {
                var testObject = RetrieveTestObject(ticket);

                logger.Info($"Successfully instantiated test object for processed ticket with ID: {ticket.Id}");

                testObjects.Add(testObject);
            }

            return testObjects;
        }

        /// <inheritdoc />
        public IEnumerable<ITestObject> RetrieveUnprocessedTestObjects()
        {
            List<ITestObject> testObjects = new List<ITestObject>();

            foreach (var ticket in TicketRepository.UnprocessedTickets)
            {
                var testObject = RetrieveTestObject(ticket);

                logger.Info($"Successfully instantiated test object for unprocessed ticket with ID: {ticket.Id}");

                testObjects.Add(testObject);
            }

            return testObjects;
        }

        /// <inheritdoc />
        public ITestObject RetrieveTestObjectById(Guid id)
        {
            ITestTicket ticket = TicketRepository.RetrieveTicketById(id);
            ITestObject testObject = RetrieveTestObject(ticket);

            logger.Info($"Successfully instantiated test object based on ticket ID: {ticket.Id}");

            return testObject;
        }

        /// <inheritdoc />
        public ITestObject RetrieveTestObjectByUrl(string url)
        {
            ITestTicket ticket = TicketRepository.RetrieveTicketByUrl(url);
            ITestObject testObject = RetrieveTestObject(ticket);

            logger.Info($"Successfully instantiated test object based on ticket ID: {ticket.Id}");

            return testObject;
        }
    }
}

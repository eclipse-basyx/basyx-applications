//Libraries
using BaSyx.AAS.Client.Http;
using BaSyx.API.Clients;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using NLog;

//Core
using TestDemonstrator.Enums;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.Ticket.Contracts;

namespace TestDemonstrator.TestObjects
{
    /// <summary>
    /// The following class serves as a concrete implementation of the <see cref="ITestObject"/> interface
    /// </summary>
    public class TestObject : ITestObject
    {
        private static readonly NLog.ILogger logger = LogManager.GetCurrentClassLogger();

        private ITestTicket ticket;

        /// <summary>
        /// The following function instantiates a test object
        /// </summary>
        /// <param name="_ticket">the test receipt</param>
        /// <exception cref="ArgumentException">if an active test receipt was passed and no AAS is reachable on the specified URI</exception>
        public TestObject(ITestTicket _ticket)
        {
            ticket = _ticket;

            if (ticket.Type == TicketType.Active)
            {
                var client = new AssetAdministrationShellHttpClient(ticket.Uri);
                Client = client;
                SubmodelClient = client;
                SubmodelRepositoryClient = client;
                HttpClient = client.HttpClient;
            }
            else if (ticket.Type == TicketType.Passive)
            {
                // just to satisfy the same interface for both active and passive test objects
                var client = new InMemoryShellClient(ticket.PassiveShell!); 
                Client = client;
                SubmodelClient = client;
                SubmodelRepositoryClient = client;
            }

            AssetAdministrationShell = Client!.RetrieveAssetAdministrationShell().Entity;

            if (AssetAdministrationShell == null)
            {
                string message = "Could not retrieve the AAS of the test object!";

                if (ticket.Type == TicketType.Active)
                {
                    message = $"Asset administration shell with URI {ticket.Uri!} of test object represented by ticket with id {ticket.Id} is unreachable!";
                }

                throw new ArgumentException(message);
            }
        }

        public ITestTicket Ticket
        {
            get => ticket;
            private set
            {
                if (value == null)
                    throw new ArgumentNullException("Invalid test ticket!");
                ticket = value;
            }
        }

        public IAssetAdministrationShell AssetAdministrationShell { get; private set; }

        public IAssetAdministrationShellClient Client { get; private set; }

        public IAssetAdministrationShellSubmodelClient SubmodelClient { get; private set; } = null!;

        public ISubmodelRepositoryClient SubmodelRepositoryClient { get; private set; } = null!;

        public HttpClient? HttpClient { get; private set; } 
    }
}

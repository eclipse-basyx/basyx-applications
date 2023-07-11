
using TestDemonstrator.Ticket.Contracts;

namespace TestDemonstrator.TicketRepository.Contracts
{
    /// <summary>
    /// The following interface implements the methods of <see cref="IRepository{T}"/>
    /// and represents a generic implementation of a ticket repository
    /// </summary>
    public interface ITicketRepository : IRepository<ITestTicket>
    {
        /// <summary>
        /// All tickets in the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns>
        IEnumerable<ITestTicket> Tickets { get; }

        /// <summary>
        /// All passive tickets in the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns>
        IEnumerable<ITestTicket> PassiveTickets { get; }

        /// <summary>
        /// All active tickets in the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns> 
        IEnumerable<ITestTicket> ActiveTickets { get; }

        /// <summary>
        /// All processed tickets in the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns>
        IEnumerable<ITestTicket> ProcessedTickets { get; }

        /// <summary>
        /// All unprocessed tickets in the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns>
        IEnumerable<ITestTicket> UnprocessedTickets { get; }

        /// <summary>
        /// The following function obtains a repository ticket, whose <see cref="ITestTicket.Id"/>  
        /// attribute corresponds to the passed argument
        /// </summary>
        /// <param name="id">The id of the ticket to retrieve</param>
        /// <returns>the test ticket as an instance implementing the <see cref="ITestTicket"/> interface</returns>
        /// <exception cref="ArgumentException">if no ticket with the given ID exists in the repository</exception>
        ITestTicket RetrieveTicketById(Guid id);

        /// <summary>
        /// The following function obtains the first active repository ticket,
        /// whose <see cref="ITestTicket.Uri"/> attribute corresponds to the passed argument
        /// </summary>
        /// <param name="uri">The URI of the ticket to retrieve</param>
        /// <returns>the test ticket as an instance implementing the <see cref="ITestTicket"/> interface</returns>
        /// <exception cref="ArgumentException">if no ticket with the given URI exists in the repository</exception>
        ITestTicket RetrieveTicketByUrl(string uri);

        /// <summary>
        /// The following function obtains all tickets from the repository
        /// </summary>
        /// <returns>a collection of instances, implementing the <see cref="ITestTicket"/> interface</returns>
        /// <exception cref="ArgumentException">if no ticket with the given ID exists in the repository</exception>
        IEnumerable<ITestTicket> RetrieveAllTickets();

        /// <summary>
        /// The following function deletes a repository ticket, whose <see cref="ITestTicket.Id"/>  
        /// attribute corresponds to the passed argument
        /// </summary>
        /// <param name="id">The id of the ticket to delete</param>
        /// <exception cref="ArgumentException">if no ticket with the given ID exists in the repository</exception>
        void DeleteTicketById(Guid id);

        /// <summary>
        /// The following function can be used to load a test report to a repository ticket
        /// </summary>
        /// <param name="id">The id of the ticket</param>
        /// <param name="report">The test report</param>
        /// <exception cref="ArgumentException">if no ticket with the given ID exists in the repository</exception>
        void UploadTestReport(Guid id, string report);
    }
}

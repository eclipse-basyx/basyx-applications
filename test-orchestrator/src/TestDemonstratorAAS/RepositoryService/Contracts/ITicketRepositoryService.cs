using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository.Contracts;

namespace TestDemonstratorAAS.RepositoryService.Contracts
{
    /// <summary>
    /// The following interface implements the functionalities of the "TicketManagement" submodel
    /// </summary>
    public interface ITicketRepositoryService
    {
        /// <summary>
        /// The ticket repository where the tickets are stored
        /// </summary>
        ITicketRepository TicketRepository { get; }

        /// <summary>
        /// A ticket builder used to construct test tickets
        /// </summary>
        ITicketBuilder TicketBuilder { get; }

        /// <summary>
        /// A descriptor factory used to construct ticket descriptors
        /// </summary>
        IDescriptorFactory DescriptorFactory { get; }

        /// <summary>
        /// Uploads an active ticket to the ticket repository
        /// </summary>
        /// <param name="uriString">the URI of the an active AAS used as a test object</param>
        /// <param name="structureContent">the XML content of the structure descriptor</param>
        /// <param name="statesContent">the XML content of the state machine descriptor</param>
        /// <returns></returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string UploadActiveTicket(string uriString, string structureContent, string statesContent);

        /// <summary>
        /// Uploads a passive ticket to the ticket repository
        /// </summary>
        /// <param name="passiveShellContent">the XML content of the passive AAS</param>
        /// <param name="structureContent">the XML content of the structure descriptor</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string UploadPassiveTicket(string passiveShellContent, string structureContent);

        /// <summary>
        /// Updates an active repository ticket 
        /// </summary>
        /// <param name="guid">the GUID of the ticket to update</param>
        /// <param name="uriString">the URI of the ticket</param>
        /// <param name="structureContent">the XML content of the structure descriptor</param>
        /// <param name="statesContent">the XML content of the state machine descriptor</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string UpdateActiveTicket(string guid, string uriString, string structureContent, string statesContent);

        /// <summary>
        /// Updates a passive repository ticket 
        /// </summary>
        /// <param name="guid">the GUID of the ticket to update</param>
        /// <param name="structureContent">the XML content of the structure descriptor</param>
        /// <param name="passiveShellContent">the XML content of the passive AAS</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string UpdatePassiveTicket(string guid, string passiveShellContent, string structureContent);

        /// <summary>
        /// Deletes a passive repository ticket 
        /// </summary>
        /// <param name="guid">the GUID of the ticket to delete</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string DeleteTicket(string guid);

        /// <summary>
        /// Retrieves a repository ticket based on a given ID
        /// </summary>
        /// <param name="guid">the GUID of the ticket to retrieve</param>
        /// <param name="ticket">the obtained ticket</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string RetrieveTicketById(string guid, ref ITestTicket? ticket);

        /// <summary>
        /// Retrieves a repository ticket based on a given URI
        /// </summary>
        /// <param name="uri">the URI of the ticket to retrieve</param>
        /// <param name="ticket">the obtained ticket</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if null or empty argument was detected</exception>
        /// <exception cref="Exception">if the operation failed</exception>
        string RetrieveTicketByUri(string uri, ref ITestTicket? ticket);

        /// <summary>
        /// Retrieves all ticket IDs 
        /// </summary>
        /// <returns>comma separated string of ticket IDs</returns>
        public string RetrieveTicketIds();
    }
}

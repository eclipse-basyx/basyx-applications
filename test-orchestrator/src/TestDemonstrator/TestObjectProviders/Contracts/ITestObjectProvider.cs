//Core
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstrator.TestObjects.Contracts;

namespace TestDemonstrator.TestObjectProviders.Contracts
{
    /// <summary>
    /// The following interface is used to read test receipts from a given
    /// ticket repository and instantiate and provide corresponding test objects
    /// </summary>
    public interface ITestObjectProvider
    {
        /// <summary>
        /// The ticket repository containing the test receipts
        /// </summary>
        ITicketRepository TicketRepository { get; }

        /// <summary>
        /// The following function instantiates a test object 
        /// based on a given test ticket instance
        /// </summary>
        /// <param name="ticket">a concrete instance of a ticket, implementing the ITestTicket interface</param>
        /// <returns>the test object, which also contains the test receipt</returns>
        ITestObject RetrieveTestObject(ITestTicket ticket);

        /// <summary>
        /// The following function instantiates a test object 
        /// based on a given test ticket identificator
        /// </summary>
        /// <param name="id">the ID of the ticket for which a test object is created</param>
        /// <returns>the test object, which also contains the test receipt</returns>
        ITestObject? RetrieveTestObjectById(Guid id);

        /// <summary>
        /// The following function instantiates a test object of the first 
        /// active ticket, whose URI attribute is the same as the passed parameter
        /// </summary>
        /// <param name="uri">the URI of the ticket for which a test object is created</param>
        /// <returns>the test object, which also contains the test receipt</returns>
        ITestObject? RetrieveTestObjectByUrl(string url);

        /// <summary>
        /// The following function instantiates a test object for
        /// each ticket in the repository
        /// </summary>
        /// <returns>an enumeration of test objects</returns>
        IEnumerable<ITestObject> RetrieveTestObjects();

        /// <summary>
        /// The following function instantiates a test object for
        /// each passive ticket in the repository
        /// </summary>
        /// <returns>an enumeration of passive test objects</returns>
        IEnumerable<ITestObject> RetrievePassiveTestObjects();

        /// <summary>
        /// The following function instantiates a test object for
        /// each active ticket in the repository
        /// </summary>
        /// <returns>an enumeration of active test objects</returns>
        IEnumerable<ITestObject> RetrieveActiveTestObjects();

        /// <summary>
        /// The following function instantiates a test object for
        /// each processed ticket in the repository
        /// </summary>
        /// <returns>an enumeration of processed test objects</returns>
        IEnumerable<ITestObject> RetrieveProcessedTestObjects();

        /// <summary>
        /// The following function instantiates a test object for
        /// each unprocessed ticket in the repository
        /// </summary>
        /// <returns>an enumeration of unprocessed test objects</returns>
        IEnumerable<ITestObject> RetrieveUnprocessedTestObjects();
    }
}

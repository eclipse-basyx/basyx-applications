//Core
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.TicketRepository.Contracts;

namespace TestDemonstrator.Orchestrator.Contracts
{
    /// <summary>
    /// The following interface is used to execute different test pipelines
    /// through the use of different ticket repositories and test frameworks
    /// </summary>
    public interface ITestOrchestrator
    {
        /// <summary>
        /// The following function executes a test pipeline for a given test object and
        /// writes the originated test protocol to the corresponding ticket in the repository
        /// </summary>
        /// <param name="testObject">the object being tested</param>
        /// <param name="repository">an instance of a ticket repository needed for storing the ticket</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecuteTestPipeline(ref ITestObject testObject, ITicketRepository repository, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// The following function executes a test pipeline for a given test object based on the
        /// corresponding ticket identificator and writes the originated test protocol to the 
        /// ticket with the gived ID
        /// </summary>
        /// <param name="ticketId">the ticket identificator as GUID</param>
        /// <param name="testObjectProvider">a test object provider instance</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        /// <exception cref="ArgumentException">if the given ticket ID is not defined in the repository</exception>
        void ExecuteTestPipelineById(Guid ticketId, ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// The following function executes a test pipeline for the first ticket with the given
        /// URI and writes the test protocol to its TestReport attribute
        /// </summary>
        /// <param name="ticketId">the AAS URI of the test object, specified also by the ticket</param>
        /// <param name="testObjectProvider">a test object provider instance</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        /// <exception cref="ArgumentException">if the given URI is not associated to a ticket in the repository</exception>
        void ExecuteTestPipelineByUri(Uri uri, ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// Executes a test pipeline for each ticket in the repository provided by the test object provider
        /// </summary>
        /// <param name="testObjectProvider">a test object provider instance, used to read all tickets in the repo</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecuteTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// Executes a test pipeline for each passive ticket in the repository provided by the test object provider
        /// </summary>
        /// <param name="testObjectProvider">a test object provider instance, used to read all passive tickets in the repo</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecutePassiveTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// Executes a test pipeline for each active ticket in the repository provided by the test object provider
        /// </summary>
        /// <param name="testObjectProvider">a test object provider instance, used to read all active tickets in the repo</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecuteActiveTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// Executes a test pipeline for each processed ticket in the repository provided by the test object provider
        /// </summary>
        /// <param name="testObjectProvider">a test object provider instance, used to read all processed tickets in the repo</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecuteProcessedTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);

        /// <summary>
        /// Executes a test pipeline for each unprocessed ticket in the repository provided by the test object provider
        /// </summary>
        /// <param name="testObjectProvider">a test object provider instance, used to read all unprocessed tickets in the repo</param>
        /// <param name="testRunner">an instance of a test runner, specifying the used test framework (NUnit etc.)</param>
        /// <param name="testClasses">a sequence of test classes, which defines the test pipeline</param>
        void ExecuteUnprocessedTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses);
    }
}

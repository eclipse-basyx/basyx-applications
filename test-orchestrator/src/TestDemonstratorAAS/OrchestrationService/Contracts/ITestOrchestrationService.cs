namespace TestDemonstratorAAS.OrchestrationService.Contracts
{
    /// <summary>
    /// The following interface defines the functionalities of the "TestExecution" submodel
    /// </summary>
    public interface ITestOrchestrationService
    {
        /// <summary>
        /// Executes a test pipeline for a test object, specified
        /// by a ticket, which id is given as an argument of the function
        /// </summary>
        /// <param name="ticketId">The id of the test ticket, whose test object serves as a subject of a test pipeline</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if an invalid argument is detected</exception>
        string ExecuteTestPipelineById(string ticketId);

        /// <summary>
        /// Executes a test pipeline for a test object, specified
        /// by a ticket, which id is given as an argument of the function
        /// </summary>
        /// <param name="ticketId">The id of the test ticket, whose test object serves as a subject of a test pipeline</param>
        /// <returns>success/error message</returns>
        /// <exception cref="ArgumentException">if an invalid argument is detected</exception>
        string ExecuteTestPipelineByUri(string uri);

        /// <summary>
        /// Executes a test pipeline for each test ticket in the repository
        /// </summary>
        /// <returns>success/error message</returns>
        string ExecuteTestPipelines();

        /// <summary>
        /// Executes a test pipeline for each active test ticket in the repository
        /// </summary>
        /// <returns>success/error message</returns>
        string ExecuteActiveTestPipelines();

        /// <summary>
        /// Executes a test pipeline for each passive test ticket in the repository
        /// </summary>
        /// <returns>success/error message</returns>
        string ExecutePassiveTestPipelines();
    }
}

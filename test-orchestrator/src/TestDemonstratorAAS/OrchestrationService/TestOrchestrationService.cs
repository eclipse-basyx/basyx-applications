//Core
using TestDemonstrator.Orchestrator.Contracts;
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstratorAAS.OrchestrationService.Contracts;

namespace TestDemonstratorAAS.OrchestrationService
{
    /// <summary>
    /// The following class serves as a concrete proof-of-concept implementation of
    /// the <see cref="ITestOrchestrationService"/> interface
    /// </summary>
    public class TestOrchestrationService : ITestOrchestrationService
    {
        private ITestOrchestrator orchestrator;

        private ITestObjectProvider testObjectProvider;

        private ITestRunner testRunner;

        private ICollection<Type> testPipeline;

        /// <summary>
        /// The following method serves as a constructor of the service class
        /// </summary>
        /// <param name="orchestrator">a concrete implementation of the <see cref="ITestOrchestrator"/> interface used to perform test pipelines</param>
        /// <param name="testObjectProvider">a concrete implementation of the <see cref="ITestObjectProvider"/> interface used to instantiate the test objects</param>
        /// <param name="testRunner">a concrete implementation of the <see cref="ITestRunner"/> interface used to execute a single test suite</param>
        /// <param name="testClasses">a sequence of test suite which defines the test pipeline</param>
        public TestOrchestrationService(ITestOrchestrator orchestrator, ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            this.testObjectProvider = testObjectProvider;
            this.testRunner = testRunner;
            this.testPipeline = new HashSet<Type>(testClasses);
            this.orchestrator = orchestrator;
        }

        /// <inheritdoc/>
        public string ExecuteTestPipelineById(string ticketId)
        {
            Guid guid;

            try
            {
                guid = new Guid(ticketId);
            }
            catch
            {
                throw new ArgumentException($"Invalid ticket Id detected!");
            }

            orchestrator.ExecuteTestPipelineById(guid, testObjectProvider, testRunner, testPipeline);
            
            return $"Successfully executed test pipeline for ticket with Id: {ticketId}";
        }

        /// <inheritdoc/>
        public string ExecuteTestPipelineByUri(string uri)
        {
            if (string.IsNullOrEmpty(uri))
                throw new ArgumentException($"Invalid Uniform Resource Identifier (URI) detected!");

            Uri url = null!;

            try
            {
                url = new Uri(uri);
            }
            catch
            {
                throw new ArgumentException($"Invalid Uniform Resource Identifier (URI) detected!");
            }

            orchestrator.ExecuteTestPipelineByUri(url, testObjectProvider, testRunner, testPipeline);

            return $"Successfully executed test pipeline for ticket with uri: {uri}";
        }

        /// <inheritdoc/>
        public string ExecuteTestPipelines()
        {
            orchestrator.ExecuteTestPipelines(testObjectProvider, testRunner, testPipeline);
            return $"Successfully executed active test pipelines.";
        }

        /// <inheritdoc/>
        public string ExecuteActiveTestPipelines()
        {
            orchestrator.ExecuteActiveTestPipelines(testObjectProvider, testRunner, testPipeline);
            return $"Successfully executed active test pipelines.";
        }

        /// <inheritdoc/>
        public string ExecutePassiveTestPipelines()
        {
            orchestrator.ExecutePassiveTestPipelines(testObjectProvider, testRunner, testPipeline);
            return $"Successfully executed passive test pipelines.";
        }

        /// <inheritdoc/>
        public string ExecuteProcessedTestPipelines()
        {
            orchestrator.ExecuteProcessedTestPipelines(testObjectProvider, testRunner, testPipeline);
            return $"Successfully executed processed test pipelines.";
        }

        /// <inheritdoc/>
        public string ExecuteUnprocessedTestPipelines()
        {
            orchestrator.ExecuteUnprocessedTestPipelines(testObjectProvider, testRunner, testPipeline);
            return $"Successfully executed unprocessed test pipelines.";
        }
    }
}

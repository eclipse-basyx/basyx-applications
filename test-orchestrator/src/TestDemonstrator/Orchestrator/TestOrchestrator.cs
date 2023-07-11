//Libraries
using NLog;
using System.Xml.Linq;

//Core
using TestDemonstrator.TestObjectProviders.Contracts;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.Orchestrator.Contracts;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.TicketRepository.Contracts;

namespace TestDemonstrator.Orchestrator
{
    /// <summary>
    /// The following class serves as a concrete implementation of the <see cref="ITestOrchestrator"> interface
    /// </summary>
    public class TestOrchestrator : ITestOrchestrator
    {
        private static readonly NLog.ILogger logger = LogManager.GetCurrentClassLogger();

        public TestOrchestrator() { }

        /// <inheritdoc />
        public void ExecuteTestPipeline(ref ITestObject testObject, ITicketRepository repository, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            var testReport = new XDocument();
            var root = new XElement("PipelineReport");

            logger.Info($"Starting test pipeline for test object with ticket id {testObject.Ticket.Id}...");

            // executing the test pipeline
            foreach (var testClass in testClasses)
            {
                logger.Info($"Executing pipeline level {testClasses.ToList().IndexOf(testClass) + 1}/{testClasses.Count}: {testClass.Name}");

                // executing the pipeline level
                var report = testRunner.RunTests(testObject, testClass);

                // writing the test report for the current pipeline level to the whole test protocol
                var element = new XElement(testClass.Name, XElement.Parse(report.OuterXml));
                root.Add(element);

                logger.Info($"Finnished pipeline level {testClasses.ToList().IndexOf(testClass) + 1}/{testClasses.Count}: {testClass.Name}");
            }

            logger.Info($"Pipeline execution completed!");

            // completing the test report and writing it to the corresponding ticket
            testReport.Add(root);
            testObject.Ticket.TestReport = XElement.Parse(testReport.ToString()).ToString();

            repository.Update(testObject.Ticket);
        }

        /// <inheritdoc />
        public void ExecuteTestPipelineById(Guid ticketId, ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            var testObject = testObjectProvider.RetrieveTestObjectById(ticketId);

            if (testObject == null)
            {
                throw new ArgumentException($"Ticket with Id {ticketId} was not found in the repository.");
            }

            ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
        }

        /// <inheritdoc />
        public void ExecuteTestPipelineByUri(Uri uri, ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            var testObject = testObjectProvider.RetrieveTestObjectByUrl(uri.OriginalString)!;

            if (testObject == null)
            {
                throw new ArgumentException($"Ticket with uri {uri} was not found in the repository.");
            }

            ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
        }

        /// <inheritdoc />
        public void ExecuteTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            testObjectProvider
                .RetrieveTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
                });
        }

        /// <inheritdoc />
        public void ExecutePassiveTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            testObjectProvider
                .RetrievePassiveTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
                });
        }

        /// <inheritdoc />
        public void ExecuteActiveTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            testObjectProvider
                .RetrieveActiveTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
                });
        }

        /// <inheritdoc />
        public void ExecuteProcessedTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            testObjectProvider
                .RetrieveProcessedTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
                });
        }

        /// <inheritdoc />
        public void ExecuteUnprocessedTestPipelines(ITestObjectProvider testObjectProvider, ITestRunner testRunner, ICollection<Type> testClasses)
        {
            testObjectProvider
                .RetrieveUnprocessedTestObjects()
                .ToList()
                .ForEach(testObject =>
                {
                    ExecuteTestPipeline(ref testObject, testObjectProvider.TicketRepository, testRunner, testClasses);
                });
        }
    }
}

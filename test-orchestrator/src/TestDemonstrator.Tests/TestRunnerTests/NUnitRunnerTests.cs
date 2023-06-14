using TestDemonstrator.Enums;
using TestDemonstrator.TestObjects;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.TestRunners;
using TestDemonstrator.TestRunners.Contracts;
using TestDemonstrator.Tests.TestRunnerTests.TestSuites;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;

namespace TestDemonstrator.Tests.TestRunnerTests
{
    public class NUnitRunnerTests
    {
        private ITestRunner? runner = null;
        private ITestObject? testObject = null;
        private ITicketBuilder? builder = null;
        private IDescriptorFactory? factory = null;
        private static string root = @"../../../DescriptorFactoryTests/TestData";

        [SetUp]
        public void Setup()
        {
            runner = new NUnitTestRunner();
            builder = new TicketBuilder();
            factory = new DescriptorFactory();

            var path = $@"{root}/PassiveShells/HTW/01_Demo_AAS.xml";
            var structureDescriptor = factory!.CreateStructureDescriptor(File.ReadAllText(path));
            var passiveShell = factory!.CreatePassiveShell(File.ReadAllText(path));

            ITestTicket ticket = builder!
                .WithType(TicketType.Passive)
                .WithStructureDescriptor(structureDescriptor)
                .WithPassiveAdministrationShell(passiveShell)
                .Build();

            testObject = new TestObject(ticket);
        }

        [Test]
        public void RunTestSuite_ExecutesTestsAndGeneratesTestReport()
        {
            var report = runner!.RunTests(testObject!, typeof(ExampleTestSuite));
            Assert.IsNotNull(report);
        }

        [Test]
        public void RunTestSuite_WithInvalidTestObject_ThrowsException()
        {
            Assert.Throws<ArgumentNullException>(() => 
                runner!.RunTests(null!, typeof(ExampleTestSuite)));
        }

        [Test]
        public void RunTestSuite_WithUnsuitableOrInvalidTestClass_ThrowsException()
        {
            Assert.Throws<ArgumentException>(() =>
               runner!.RunTests(testObject!, typeof(int)));
        }
    }
}

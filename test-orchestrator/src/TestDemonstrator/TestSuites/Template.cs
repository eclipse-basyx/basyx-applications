using NUnit.Framework;
using System.Diagnostics;

using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.TestRunners;

namespace TestDemonstrator.TestSuites
{
    public class Template
    {
        private ITestObject testObject = null!;
        
        [OneTimeSetUp]
        public void OneTimeSetUp()
        {
            using var listener = new ConsoleTraceListener()
            {
                Name = $"{GetType().Name}TraceListener"
            };

            Trace.Listeners.Add(listener);
            testObject = TestObjectFetcher.CurrentTestObject;
        }

        [SetUp]
        public void SetUp()
        { 

        }

        [Test]
        public void Test()
        { 
        
        }

        [TearDown]
        public void TearDown()
        {

        }

        [OneTimeTearDown]
        public void OneTimeTearDown()
        {
            Trace.Flush();
            Trace.Listeners.Remove($"{GetType().Name}TraceListener");
        }
    }
}

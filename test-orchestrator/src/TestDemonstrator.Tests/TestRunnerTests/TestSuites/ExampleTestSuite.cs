using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestDemonstrator.Tests.TestRunnerTests.TestSuites
{
    public class ExampleTestSuite
    {
        [Test]
        public static void PassingTest() 
        { 
            Assert.IsTrue(true);
        }

        //[Test] In order for all tests to succeed
        public static void FailingTest()
        {
            Assert.IsTrue(false);
        }
    }
}

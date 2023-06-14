using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestDemonstrator.Tests.TestOrchestratorTests.TestSuites
{
    public class ExamplePipelineLevel03
    {
        [Test]
        public static void PassingTest()
        {
            Assert.IsTrue(true);
        }

        [Test]
        public static void FailingTest()
        {
            Assert.IsTrue(false);
        }
    }
}

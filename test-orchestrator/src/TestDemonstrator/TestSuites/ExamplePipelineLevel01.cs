using NUnit.Framework;

namespace TestDemonstrator.TestSuites
{
    public class ExamplePipelineLevel01
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

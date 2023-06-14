
using System.Xml;
using TestDemonstrator.TestObjects.Contracts;

namespace TestDemonstrator.TestRunners.Contracts
{
    /// <summary>
    /// The following interface is used to execute a single
    /// test suite (pipeline level)
    /// </summary>
    public interface ITestRunner
    {
        /// <summary>
        /// The following function executes a test suite
        /// on the given test object
        /// </summary>
        /// <param name="testObject">the test object</param>
        /// <param name="type">the type of the class implementing the test suite</param>
        /// <returns>a test protocol as an xml node</returns>
        /// <exception cref="ArgumentException">if the test class does not implement any tests</exception>
        XmlNode RunTests(ITestObject testObject, Type testClass);
    }
}

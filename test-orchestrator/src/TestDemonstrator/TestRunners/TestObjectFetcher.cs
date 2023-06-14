
using TestDemonstrator.TestObjects.Contracts;

namespace TestDemonstrator.TestRunners
{
    /// <summary>
    /// The class is used from the test suites to fetch the
    /// current test object. There is currently no other
    /// way of passing the test object to the test suite.
    /// </summary>
    public class TestObjectFetcher
    {
        private static ITestObject _currentTestObject = default!;

        public static ITestObject CurrentTestObject
        {
            get
            {
                if (_currentTestObject == null)
                    throw new ArgumentNullException();
                return _currentTestObject;
            }

            set
            {
                if (value == null)
                    throw new ArgumentNullException();
                _currentTestObject = value;
            }
        }
    }
}

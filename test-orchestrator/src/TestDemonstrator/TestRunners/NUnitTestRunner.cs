using NUnitLite;
using NUnit;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TestDemonstrator.TestObjects;
using NUnit.Engine;
using System.Reflection;
using System.Xml;
using System.IO;
using System.Xml.Linq;
using NUnit.Common;
using TestDemonstrator.TestObjects.Contracts;
using NLog;

namespace TestDemonstrator.TestRunners
{
    /// <summary>
    /// The following class serves as a runner of test suites 
    /// implemented with the NUnit test framework
    /// </summary>
    public class NUnitTestRunner : Contracts.ITestRunner
    {
        private static readonly NLog.ILogger logger = LogManager.GetCurrentClassLogger();

#if false
        //Alternative method, no test protocol provided
        public int RunTests(ITestObject testObject, Type type)
        {
            TestObjectFetcher.CurrentTestObject = testObject;
            string[] args = new string[] { $"--test={type.FullName}" };
            // int result = new AutoRun().Execute(args);
            int result = new AutoRun().Execute(args, new ExtendedTextWrapper(Console.Out), Console.In);

            return result;
        }
#endif

        public XmlNode RunTests(ITestObject testObject, Type type)
        {
            TestObjectFetcher.CurrentTestObject = testObject;
            var testPackage = new TestPackage(Assembly.GetAssembly(type)!.Location);
            testPackage.AddSetting("WorkDirectory", Environment.CurrentDirectory);
            using var engine = TestEngineActivator.CreateInstance();

#if false     
            //Another way to specify the filter
            var filterService = engine.Services.GetService<ITestFilterService>();
            var builder = filterService.GetTestFilterBuilder();
            builder.AddTest(type.FullName);
            var filter = builder.GetFilter();
#endif

            using var runner = engine.GetRunner(testPackage);
            var filter = new TestFilter($"<filter><class>{type.FullName}</class></filter>");
            int testsCount = runner.CountTestCases(filter);

            if (testsCount == 0)
            {
                throw new ArgumentException("The type passed doesn't implement any tests!");
            }

            XmlNode result = runner.Run(null, filter);
            
            return result; // XElement.Parse(result.OuterXml).ToString(); 
        }
    }
}

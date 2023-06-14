using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using NUnit.Framework;

using System.Diagnostics;
using System.Globalization;

using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.TestRunners;

namespace TestDemonstrator.TestSuites
{
    [TestFixture]
    public class StructureTests
    {
        private ITestObject testObject = null!;
        private List<ISubmodel> envSubmodels = null!;

        [OneTimeSetUp]
        public void OneTimeSetUp()
        {
            using var listener = new ConsoleTraceListener()
            {
                Name = "StructureTestsTraceListener"
            };
            Trace.Listeners.Add(listener);

            testObject = TestObjectFetcher.CurrentTestObject;
            IAssetAdministrationShell environment = testObject.Ticket
                .StructureDescriptor!
                .Instance;

            envSubmodels = environment.Submodels.ToList();
        }

        [OneTimeTearDown]
        public void OneTimeTearDown()
        {
            Trace.Flush();
            Trace.Listeners.Remove("StructureTestsTraceListener"); 
        }

        private static void Report(string message)
        {
            Trace.WriteLine($"{DateTime.UtcNow.ToString("yyyy-MM-dd HH:mm:ss.fff", CultureInfo.InvariantCulture)} " + message);
        }

        [Test]
        public void TestStructure()
        {
            //Obtaining all submodels of the AAS of the test object
            var submodels = testObject.SubmodelRepositoryClient
                .RetrieveSubmodels()
                .Entity;

            Assert.IsNotNull(submodels, $"The submodels of the test object could not be obtained! The URI-Adress is probably not active anymore.");

            //Testing if the count of the submodels of the test object corrseponds to the submodels count defined in the structure descriptor
            Assert.AreEqual(submodels.Count, envSubmodels.Count);
            
            Trace.IndentLevel+=5;
            Trace.WriteLine(null);

            Report($"Expected submodels count:  {envSubmodels.Count}");
            Report($"Actual submodels count:  {submodels.Count}");

            //foreach submodel
            for (int i = 0; i < envSubmodels.Count; i++)
            {
                //Check if the IdShort of each submodel corrseponds to the IdShort in the structure descriptor
                Assert.AreEqual(submodels[i].IdShort, envSubmodels[i].IdShort, 
                    $"The number of submodels of the AAS doesn't match the one in the structure descriptor! " +
                    $"Expected: {submodels[i].IdShort} Actual: {envSubmodels[i].IdShort}");

                //TODO Check semantic IDs

                Report($"Expected IdShort of submodel number {i}: {envSubmodels[i].IdShort}");
                Report($"Actual IdShort of submodel number {i}:   {submodels[i].IdShort}");

                //Check if the number of submodel elements of each submodel corresponds to the number of submodel elements in the structure descriptor
                Assert.AreEqual(submodels[i].SubmodelElements.Count, envSubmodels[i].SubmodelElements.Count,
                    $"The number of elements of submodel Nr. ${i} doesn't match the one in the structure descriptor! " +
                    $"Expected: {submodels[i].SubmodelElements.Count} Actual: {envSubmodels[i].SubmodelElements.Count}");

                Report($"Expected submodel elements count: {envSubmodels[i].SubmodelElements.Count}");
                Report($"Actual submodel elements count:   {submodels[i].SubmodelElements.Count}");

                //If the current submodel has any elements
                if (submodels[i].SubmodelElements.Count > 0)
                {
                    //For each one of them
                    for (int j = 0; j < submodels[i].SubmodelElements.Count; j++)
                    {
                        TraverseAndTestElementCollection(submodels[i].SubmodelElements[j], envSubmodels[i].SubmodelElements[j], j, "");
                    }
                }
            }

            Trace.WriteLine(null);
            Report($"Test passed!");
        }

        private static void TraverseAndTestElementCollection(ISubmodelElement submodelElementObj, ISubmodelElement submodelElementEnv, int idx, string space)
        {
            string sp = space + "\t";

            //Check if the Id of the AAS entity matches the Id in the test configuration
            Assert.AreEqual(submodelElementObj.IdShort, submodelElementEnv.IdShort,
                   $"Identification string missmatch! " +
                   $"Expected: {submodelElementObj.IdShort} Actual: {submodelElementEnv.IdShort}");

            Report($"{sp}Expected IdShort of submodel element {idx}: {submodelElementEnv.IdShort}");
            Report($"{sp}Actual IdShort of submodel element {idx}:   {submodelElementObj.IdShort}");

            //If the entity is an element collection
            if (submodelElementEnv is ISubmodelElementCollection)
            {
                if (!(submodelElementObj is ISubmodelElementCollection))
                {
                    Report($"{sp}Submodel element {submodelElementObj.IdShort} is not an element collection");
                    Assert.Fail($"Type missmatch! " +
                        $"{submodelElementEnv.IdShort} is submodel element collection! " +
                        $"{submodelElementObj.IdShort} is not!");
                }

                Report($"{sp}Submodel element {submodelElementObj.IdShort} is an element collection");

                //Assert that the element collection of the test object and the element collection
                //in the structure descriptor have same number of elements
                Assert.AreEqual(
                    (submodelElementEnv as ISubmodelElementCollection)?.Value.Count,
                    (submodelElementObj as ISubmodelElementCollection)?.Value.Count,
                    $"{sp}The number of elements of submodel element collection with ID {submodelElementEnv.IdShort} " +
                    $"doesn't match the number of elements of submodel element collection with ID {submodelElementObj.IdShort} in the structure descriptor!");

                Report($"{sp}{submodelElementEnv.IdShort}: Expected elements count: {(submodelElementEnv as ISubmodelElementCollection)?.Value.Count}");
                Report($"{sp}{submodelElementEnv.IdShort}: Actual submodel elements count: {(submodelElementObj as ISubmodelElementCollection)?.Value.Count}");

                if ((submodelElementObj as ISubmodelElementCollection)?.Value.Count > 0)
                {
                    //Perform the test for each child
                    for (int i = 0; i < (submodelElementObj as ISubmodelElementCollection)?.Value.Count; i++)
                    {
                        TraverseAndTestElementCollection(
                            (submodelElementObj as ISubmodelElementCollection)!.Value[i],
                            (submodelElementEnv as ISubmodelElementCollection)!.Value[i], i, sp);
                    }
                }
            }
        }
    }
}

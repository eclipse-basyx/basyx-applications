using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using NUnit.Framework;

using System.Diagnostics;
using System.Globalization;

using TestDemonstrator.Enums;
using TestDemonstrator.TestObjects.Contracts;
using TestDemonstrator.TestRunners;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.TestSuites
{
    public class StateMachineTests
    {
        private ITestObject testObject = null!;
        private IStateMachineDescriptor stateMachineDescriptor = null!;

        [OneTimeSetUp]
        public void OneTimeSetUp()
        {
            testObject = TestObjectFetcher.CurrentTestObject;
            if (testObject.Ticket.Type == TicketType.Passive || testObject.Ticket.StateMachineDescriptor == null)
            {
                Assert.Ignore();
            }

            var listener = new ConsoleTraceListener()
            {
                Name = "StateMachineTraceListener"
            };

            Trace.Listeners.Add(listener);
            stateMachineDescriptor = testObject.Ticket.StateMachineDescriptor!;
        }

        [OneTimeTearDown]
        public void OneTimeTearDown()
        {
            Trace.Flush();
            Trace.Listeners.Remove("StateMachineTraceListener");
        }

        private static string getValue(int value)
        {
            switch (value)
            {
                case 0: return "nocommand";
                case 1: return "Reset";
                case 2: return "Start";
                case 3: return "Stop";
                case 4: return "Hold";
                case 5: return "Unhold";
                case 6: return "Suspend";
                case 7: return "Unsuspend";
                case 8: return "Abort";
                case 9: return "Clear";

            }
            throw new ArgumentException("Unknown value requested");
        }

        private static int getValue(string value)
        {
            switch (value)
            {
                case "nocommand": return 0;
                case "Reset": return 1;
                case "Start": return 2;
                case "Stop": return 3;
                case "Hold": return 4;
                case "Unhold": return 5;
                case "Suspend": return 6;
                case "Unsuspend": return 7;
                case "Abort": return 8;
                case "Clear": return 9;

            }
            throw new ArgumentException("Unknown value requested");
        }

        private void Report(string message)
        {
            Trace.WriteLine($"{DateTime.UtcNow.ToString("yyyy-MM-dd HH:mm:ss.fff", CultureInfo.InvariantCulture)} " + message);
        }


        [Test, Order(1)]
        public void TestStructureDependencies()
        {
            Trace.IndentLevel += 5;

            //Checking if data in pne time setup section is valid
            if (stateMachineDescriptor.Onetimesetup.Actions != null)
            {
                foreach (var action in stateMachineDescriptor.Onetimesetup.Actions)
                {
                    string submodelId = action.Submodel!;
                    string setterPropertyId = action.Setter!;
                    string getterPropertyId = action.Getter!;

                    int actionNumber = stateMachineDescriptor.Onetimesetup.Actions.IndexOf(action) + 1;
                    var submodel = testObject.SubmodelClient.RetrieveSubmodel(submodelId).Entity;
                    var getterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, getterPropertyId).Entity;
                    var setterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, setterPropertyId).Entity;

                    Assert.IsNotNull(submodel, $"Submodel with ID ${submodelId}, specified as part of the onetimesetup section, action number {actionNumber}, doesn't exist!");
                    Assert.IsNotNull(getterProp, $"Submodel element with with ID ${setterPropertyId}, specified as part of the onetimesetup section, action number {actionNumber}, doesn't exist!");
                    Assert.IsNotNull(setterProp, $"Submodel element with ID ${getterPropertyId}, specified as part of the onetimesetup section, action number {actionNumber}, doesn't exist!");
                }
            }

            //Checking if path data is valid
            foreach (var path in stateMachineDescriptor.Paths.PathCollection!)
            {
                //Checking if data in setup section is valid
                if (path.Setup != null)
                {
                    foreach (var action in path.Setup.Actions!)
                    {
                        int actionNumber = path.Setup.Actions!.IndexOf(action) + 1;
                        var sm = testObject.SubmodelClient.RetrieveSubmodel(action.Submodel).Entity;
                        var getProp = testObject.SubmodelClient.RetrieveSubmodelElement(action.Submodel, action.Getter).Entity;
                        var setProp = testObject.SubmodelClient.RetrieveSubmodelElement(action.Submodel, action.Setter).Entity;

                        Assert.IsNotNull(sm, $"Submodel with ID ${action.Submodel}, specified as part of the path setup section, action number {actionNumber}, doesn't exist!");
                        Assert.IsNotNull(getProp, $"Submodel element with with ID ${action.Getter}, specified as part of the setup section, action number {actionNumber}, doesn't exist as part of submodel with ID ${action.Submodel}!");
                        Assert.IsNotNull(setProp, $"Submodel element with with ID ${action.Setter}, specified as part of the setup section, action number {actionNumber}, doesn't exist as part of submodel with ID ${action.Submodel}!");
                    }
                }

                //Checking if properties data is valid
                string submodelId = path.Properties!.Submodel!;
                string getterPropertyId = path.Properties!.Getter!;
                string setterPropertyId = path.Properties!.Setter!;

                var submodel = testObject.SubmodelClient.RetrieveSubmodel(submodelId).Entity;
                var getterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, getterPropertyId).Entity;
                var setterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, setterPropertyId).Entity;

                Assert.IsNotNull(submodel, $"Submodel with ID ${submodelId}, specified as part of the path properties section doesn't exist!");
                Assert.IsNotNull(getterProp, $"Submodel element with with ID ${getterPropertyId}, specified as part of the path properties section doesn't exist as part of submodel with ID ${submodelId}!");
                Assert.IsNotNull(setterProp, $"Submodel element with with ID ${setterPropertyId}, specified as part of the path properties section doesn't exist as part of submodel with ID ${submodelId}!");

                //Checking if data in teardown section is valid
                if (path.Teardown != null)
                {
                    foreach (var action in path.Teardown.Actions!)
                    {
                        int actionNumber = path.Teardown.Actions!.IndexOf(action) + 1;
                        var sm = testObject.SubmodelClient.RetrieveSubmodel(action.Submodel).Entity;
                        var getProp = testObject.SubmodelClient.RetrieveSubmodelElement(action.Submodel, action.Getter).Entity;
                        var setProp = testObject.SubmodelClient.RetrieveSubmodelElement(action.Submodel, action.Setter).Entity;

                        Assert.IsNotNull(sm, $"Submodel with ID ${action.Submodel}, specified as part of the teardown section, action number {actionNumber}, doesn't exist!");
                        Assert.IsNotNull(getProp, $"Submodel element with with ID ${action.Getter}, specified as part of the teardown section, action number {actionNumber}, doesn't exist as part of submodel with ID ${action.Submodel}!");
                        Assert.IsNotNull(setProp, $"Submodel element with with ID ${action.Setter}, specified as part of the teardown section, action number {actionNumber}, doesn't exist as part of submodel with ID ${action.Submodel}!");

                    }
                }
            }

            //Checking if data in one time teardown section is valid
            if (stateMachineDescriptor.Onetimeteardown != null)
            {
                foreach (var action in stateMachineDescriptor.Onetimeteardown.Actions!)
                {
                    string submodelId = action.Submodel!;
                    string setterPropertyId = action.Setter!;
                    string getterPropertyId = action.Getter!;

                    int actionNumber = stateMachineDescriptor.Onetimeteardown.Actions!.IndexOf(action) + 1;
                    var submodel = testObject.SubmodelClient.RetrieveSubmodel(submodelId).Entity;
                    var getterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, getterPropertyId).Entity;
                    var setterProp = testObject.SubmodelClient.RetrieveSubmodelElement(submodelId, setterPropertyId).Entity;

                    Assert.IsNotNull(submodel, $"Submodel with ID ${submodelId}, specified as part of the onetimeteardown section, action number {actionNumber}, doesn't exist!");
                    Assert.IsNotNull(getterProp, $"Submodel element with with ID ${setterPropertyId}, specified as part of the onetimeteardown section, action number {actionNumber}, doesn't exist!");
                    Assert.IsNotNull(setterProp, $"Submodel element with ID ${getterPropertyId}, specified as part of the onetimeteardown section, action number {actionNumber}, doesn't exist!");
                }
            }

            Report($"Descriptor data check executed successfully!");
        }

        [Test, Order(2)]
        public void TraverseStateMachine()
        {
            //Executing one time setup actions
            if (stateMachineDescriptor!.Onetimesetup != null)
            {
                Report($"Executing one time setup actions...");

                foreach (var action in stateMachineDescriptor.Onetimesetup.Actions!)
                {
                    //Obtaining the specified descriptor values
                    int actionNumber = stateMachineDescriptor.Onetimesetup.Actions!.IndexOf(action) + 1;
                    (string submodelId, string setterPropertyId, string getterPropertyId) = (action.Submodel!, action.Setter!, action.Getter!);
                    (string value, string valueType) = (action.Set!, action.SetType!);
                    (string expected, string expectedType) = (action.Expect!, action.ExpectType!);
                    int delay = action.Wait!;

                    //Parsing the values
                    ElementValue expectedValue = new ElementValue(expected, Type.GetType(expectedType));
                    ElementValue elementValue = new ElementValue(value, Type.GetType(valueType));

                    //Setting the specified submodel element
                    Report($"Setting property {setterPropertyId} of submodel {submodelId} to {value}");
                    testObject.SubmodelClient.UpdateSubmodelElementValue(submodelId, setterPropertyId, elementValue);

                    //Waiting for the action to occur
                    Task.Delay(delay).Wait();

                    //Retrieving the specified submodel element
                    var currentValue = testObject.SubmodelClient
                            .RetrieveSubmodelElementValue(submodelId, getterPropertyId)
                            .Entity
                            .Value;

                    //Testing if the action was completed as defined
                    Assert.AreEqual(expectedValue.Value, currentValue, $"Missmatch between expected and current value of onetimesetup action number ${actionNumber}! Expected: {expectedValue.Value} Current: {currentValue}");
                    Report($"Property {setterPropertyId} value of submodel {submodelId} equals to {currentValue}");
                }
            }

            //Starting to test each specified path
            foreach (var path in stateMachineDescriptor.Paths.PathCollection!)
            {
                Trace.WriteLine(null);
                int pathNumber = stateMachineDescriptor.Paths.PathCollection!.IndexOf(path) + 1;
                Report($"Path: {pathNumber}");

                //Executing the path setup actions
                if (path.Setup != null)
                {
                    Report($"Executing path setup actions...");

                    foreach (var action in path.Setup.Actions!)
                    {
                        //Obtaining the specified descriptor values
                        int actionNumber = path.Setup.Actions!.IndexOf(action) + 1;
                        (string submodelId, string setterPropertyId, string getterPropertyId) = (action.Submodel!, action.Setter!, action.Getter!);
                        (string value, string valueType) = (action.Set!, action.SetType!);
                        (string expected, string expectedType) = (action.Expect!, action.ExpectType!);
                        int delay = action.Wait!;

                        //Parsing the values
                        ElementValue expectedValue = new ElementValue(expected, Type.GetType(expectedType));
                        ElementValue elementValue = new ElementValue(value, Type.GetType(valueType));

                        //Setting the specified submodel element
                        Report($"   Setting property {setterPropertyId} of submodel {submodelId} to {elementValue.Value}");
                        testObject.SubmodelClient.UpdateSubmodelElementValue(submodelId, setterPropertyId, elementValue);

                        //Waiting dor the action to occur
                        Task.Delay(delay).Wait();

                        //Retrieving the specified submodel element
                        var currentValue = testObject.SubmodelClient
                                .RetrieveSubmodelElementValue(submodelId, getterPropertyId)
                                .Entity
                                .Value;

                        //Testing if the action was completed as defined
                        Assert.AreEqual(expectedValue.Value, currentValue, $"Missmatch between expected and current value of setup action number ${actionNumber}! Expected: {expectedValue.Value} Current: {currentValue}");
                        Report($"   Property {setterPropertyId} value of submodel {submodelId} equals to {currentValue}");
                    }
                }

                Report($"Starting path execution...");

                foreach (var state in path.State!)
                {
                    int stateNumber = path.State!.IndexOf(state) + 1;
                    string submodelId = path.Properties!.Submodel!;
                    string getterPropertyId = path.Properties!.Getter!;
                    string setterPropertyId = path.Properties!.Setter!;
                    int delay = state.Transition!.Wait!;

                    Report($@"   Testing transition from {state.Id!} to {state.Transition.Target!}");
                    Report($@"   Sending event {state.Transition.Event!}");

                    // Set state
                    ElementValue elementValue = new ElementValue(getValue(state.Transition.Event!), typeof(int));
                    testObject.SubmodelClient.UpdateSubmodelElementValue(submodelId, setterPropertyId, elementValue);

                    // Waiting for the transition to occur
                    Task.Delay(delay).Wait();

                    // Check state
                    var actualState = testObject.SubmodelClient
                        .RetrieveSubmodelElementValue(submodelId, getterPropertyId)
                        .Entity
                        .Value;

                    Assert.AreEqual(state.Transition.Target, actualState, $"Missmatch between target and current state of state section number {stateNumber} in path number {pathNumber}!");
                    Report($"   Expected state: {state.Transition.Target}, Current state: {actualState}");
                }

                //Executing the path teardown actions
                if (path.Teardown != null)
                {
                    Report($"Executing path teardown actions...");

                    foreach (var action in path.Teardown.Actions!)
                    {
                        //Obtaining the specified descriptor values
                        int actionNumber = path.Teardown.Actions!.IndexOf(action) + 1;
                        (string submodelId, string setterPropertyId, string getterPropertyId) = (action.Submodel!, action.Setter!, action.Getter!);
                        (string value, string valueType) = (action.Set!, action.SetType!);
                        (string expected, string expectedType) = (action.Expect!, action.ExpectType!);
                        int delay = action.Wait!;

                        //Parsing the values
                        ElementValue expectedValue = new ElementValue(expected, Type.GetType(expectedType));
                        ElementValue elementValue = new ElementValue(value, Type.GetType(valueType));

                        //Setting the specified submodel element
                        Report($"   Setting property {setterPropertyId} of submodel {submodelId} to {value}");
                        testObject.SubmodelClient.UpdateSubmodelElementValue(submodelId, setterPropertyId, elementValue);

                        //Waiting dor the action to occur
                        Task.Delay(delay).Wait();

                        //Retrieving the specified submodel element
                        var currentValue = testObject.SubmodelClient
                                .RetrieveSubmodelElementValue(submodelId, getterPropertyId)
                                .Entity
                                .Value;

                        //Testing if the action was completed as defined
                        Assert.AreEqual(expectedValue.Value, currentValue, $"Missmatch between expected and current value of teardown action number ${actionNumber}! Expected: {expectedValue.Value} Current: {currentValue}");
                        Report($"   Property {setterPropertyId} value of submodel {submodelId} equals to {currentValue}");
                    }
                }

                Report($"Path passed!");
            }

            Trace.WriteLine(null);

            //Executing one time teardown actions
            if (stateMachineDescriptor!.Onetimeteardown != null)
            {
                Report($"Executing one time teardown actions...");

                foreach (var action in stateMachineDescriptor.Onetimeteardown.Actions!)
                {
                    //Obtaining the specified descriptor values
                    int actionNumber = stateMachineDescriptor.Onetimeteardown.Actions!.IndexOf(action) + 1;
                    (string submodelId, string setterPropertyId, string getterPropertyId) = (action.Submodel!, action.Setter!, action.Getter!);
                    (string value, string valueType) = (action.Set!, action.SetType!);
                    (string expected, string expectedType) = (action.Expect!, action.ExpectType!);
                    int delay = action.Wait!;

                    //Parsing the values
                    ElementValue expectedValue = new ElementValue(expected, Type.GetType(expectedType));
                    ElementValue elementValue = new ElementValue(value, Type.GetType(valueType));

                    //Setting the specified submodel element
                    Report($"Setting property {setterPropertyId} of submodel {submodelId} to {value}");
                    testObject.SubmodelClient.UpdateSubmodelElementValue(submodelId, setterPropertyId, elementValue);

                    //Waiting dor the action to occur
                    Task.Delay(delay).Wait();

                    //Retrieving the specified submodel element
                    var currentValue = testObject.SubmodelClient
                            .RetrieveSubmodelElementValue(submodelId, getterPropertyId)
                            .Entity
                            .Value;

                    //Testing if the action was completed as defined
                    Assert.AreEqual(expectedValue.Value, currentValue, $"Missmatch between expected and current value of onetimeteardown action number ${actionNumber}! Expected: {expectedValue.Value} Current: {currentValue}");
                    Report($"Property {setterPropertyId} value of submodel {submodelId} equals to {currentValue}");
                }
            }

            Trace.WriteLine(null);
            Report($"Test passed!");
        }
    }
}

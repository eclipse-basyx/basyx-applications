//Libraries
using NLog;
using BaSyx.Models.Core.AssetAdministrationShell;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Core.AssetAdministrationShell.Identification;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using BaSyx.Models.Core.Common;
using BaSyx.Models.Extensions;
using BaSyx.Utils.ResultHandling;

//Core
using TestDemonstrator.Ticket.Contracts;
using TestDemonstratorAAS.OrchestrationService.Contracts;
using TestDemonstratorAAS.RepositoryService.Contracts;

namespace TestDemonstratorAAS.AdministrationShell
{
    /// <summary>
    /// The following class implements the asset administration shell of the test automation solution
    /// </summary>
    public class TestManagementShell
    {
        private static readonly NLog.ILogger logger = LogManager.GetCurrentClassLogger();

        private ITestOrchestrationService orchestrationService;

        private ITicketRepositoryService repositoryService;

        private IAssetAdministrationShell administrationShell;

        /// <summary>
        /// The following method serves as a constructor of the current class
        /// </summary>
        /// <param name="orchestrationService">a concrete implementation of the <see cref="ITestOrchestrationService"/> 
        /// interface, used to implement the "TestExecution" submodel</param>
        /// <param name="repositoryService">a concrete implementation of the <see cref="ITicketRepositoryService"/> interface,
        /// used to implement the "TicketManagement" submodel</param>
        public TestManagementShell(ITestOrchestrationService orchestrationService, ITicketRepositoryService repositoryService)
        {
            this.orchestrationService = orchestrationService;

            this.repositoryService = repositoryService;

            this.administrationShell = RetrieveDefaultInitialization(this.GetType().Name, new Identifier(this.GetType().Name, KeyType.IRI));
        }

        public ITestOrchestrationService CoordinatorService
        {
            get => orchestrationService;
            private set
            {
                if (value == null)
                    throw new ArgumentNullException();
                orchestrationService = value;
            }
        }

        public ITicketRepositoryService RepositoryService
        {
            get => repositoryService;
            private set
            {
                if (value == null)
                    throw new ArgumentNullException();
                repositoryService = value;
            }
        }

        public IAssetAdministrationShell AdministrationShell
        {
            get => administrationShell;
            private set
            {
                if (value == null)
                    throw new ArgumentNullException();
                administrationShell = value;
            }
        }

        /// <summary>
        /// The following method builds the AAS and integrates the submodels
        /// </summary>
        /// <param name="idShort">The short ID of the AAS</param>
        /// <param name="identificaiton">The AAS identifier</param>
        /// <returns></returns>
        private IAssetAdministrationShell RetrieveDefaultInitialization(string idShort, Identifier identificaiton)
        {
            AssetAdministrationShell aas = new AssetAdministrationShell(idShort, identificaiton)
            {
                Description = new LangStringSet()
                {
                    new LangString("de-DE", "Verwaltungsschale zum Testen von Industrie 4.0 Komponenten (Prototyp)"),
                    new LangString("en-US", "Asset administration shell for verification and validation of Industrie 4.0 components (proof-of-concept implementation)")
                },
                Administration = new AdministrativeInformation()
                {
                    Version = "0.0",
                    Revision = "0"
                },
                Asset = new Asset("TestDemonstrator", new Identifier($"http://htw-berlin.de/assets/TestDemonstrator/{Guid.NewGuid()}", KeyType.IRI))
                {
                    Kind = AssetKind.Instance,
                    Description = new LangStringSet()
                    {
                        new LangString("de-DE", "Softwarekomponente Test Orchestrator"),
                        new LangString("en-US", "Software component test demonstrator")
                    }
                }
            };

            aas.Submodels.Add(TicketManagementSubmodel());
            aas.Submodels.Add(TestExecutionSubmodel());

            return aas;
        }

        /// <summary>
        /// The following method implements the submodel elements of the "TicketManagement" submodel
        /// </summary>
        /// <returns>The created submodel</returns>
        private ISubmodel TicketManagementSubmodel()
        {
            ISubmodel ticketRepositorySubmodel = new Submodel("TicketManagement", new Identifier("TicketManagement", KeyType.IRI));

            ticketRepositorySubmodel.SubmodelElements.Add(TicketsCountProperty("TicketsCount"));
            ticketRepositorySubmodel.SubmodelElements.Add(PassiveTicketsCountProperty("PassiveTicketsCount"));
            ticketRepositorySubmodel.SubmodelElements.Add(ActiveTicketsCountProperty("ActiveTicketsCount"));
            ticketRepositorySubmodel.SubmodelElements.Add(ProcessedTicketsCountProperty("ProcessedTicketsCount"));
            ticketRepositorySubmodel.SubmodelElements.Add(UnprocessedTicketsCountProperty("UnprocessedTicketsCount"));
            ticketRepositorySubmodel.SubmodelElements.Add(UploadActiveTicketOperation("UploadActiveTicket"));
            ticketRepositorySubmodel.SubmodelElements.Add(UploadPassiveTicketOperation("UploadPassiveTicket"));
            ticketRepositorySubmodel.SubmodelElements.Add(UpdateActiveTicketOperation("UpdateActiveTicket"));
            ticketRepositorySubmodel.SubmodelElements.Add(UpdatePassiveTicketOperation("UpdatePassiveTicket"));
            ticketRepositorySubmodel.SubmodelElements.Add(DeleteTicketOperation("DeleteTicket"));
            ticketRepositorySubmodel.SubmodelElements.Add(RetrieveTicketByIdOperation("RetrieveTicketById"));
            ticketRepositorySubmodel.SubmodelElements.Add(RetrieveTicketByUriOperation("RetrieveTicketByUri"));
            ticketRepositorySubmodel.SubmodelElements.Add(RetrieveTicketIdsOperation("RetrieveTicketIdentificators"));

            ISubmodelElement TicketsCountProperty(string idShort)
            {
                ISubmodelElement property = new Property<int>(idShort)
                {
                    Set = null,
                    Get = prop => repositoryService.TicketRepository.Tickets.Count(),
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Obtains the number of tickets in the ticket repository"),
                        new LangString("DE", "Gibt die Anzahl von Test-Rezepten im Ticketverzeichnis an"),
                    },
                };

                return property;
            }
            ISubmodelElement PassiveTicketsCountProperty(string idShort)
            {
                ISubmodelElement property = new Property<int>(idShort)
                {
                    Set = null,
                    Get = prop => repositoryService.TicketRepository.PassiveTickets.Count(),
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Obtains the number of passive tickets in the ticket repository"),
                        new LangString("DE", "Gibt die Anzahl von passiven Test-Rezepten im Ticketverzeichnis an"),
                    },
                };

                return property;
            }
            ISubmodelElement ActiveTicketsCountProperty(string idShort)
            {
                ISubmodelElement property = new Property<int>(idShort)
                {
                    Set = null,
                    Get = prop => repositoryService.TicketRepository.ActiveTickets.Count(),
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Obtains the number of active tickets in the ticket repository"),
                        new LangString("DE", "Gibt die Anzahl von aktiven Test-Rezepten im Ticketverzeichnis an"),
                    },
                };

                return property;
            }
            ISubmodelElement ProcessedTicketsCountProperty(string idShort)
            {
                ISubmodelElement property = new Property<int>(idShort)
                {
                    Set = null,
                    Get = prop => repositoryService.TicketRepository.ProcessedTickets.Count(),
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Obtains the number of processed tickets in the ticket repository"),
                        new LangString("DE", "Gibt die Anzahl von bearbeiteten Test-Rezepten im Ticketverzeichnis an"),
                    },
                };

                return property;
            }
            ISubmodelElement UnprocessedTicketsCountProperty(string idShort)
            {
                ISubmodelElement property = new Property<int>(idShort)
                {
                    Set = null,
                    Get = prop => repositoryService.TicketRepository.UnprocessedTickets.Count(),
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Obtains the number of unprocessed tickets in the ticket repository"),
                        new LangString("DE", "Gibt die Anzahl von nicht bearbeiteten Test-Rezepten im Ticketverzeichnis an"),
                    },
                };

                return property;
            }
            ISubmodelElement UploadActiveTicketOperation(string idShort)
            {
                const string INARG_URI_ID_SHORT = "Uri";
                const string INARG_STRUCT_CONTENT_ID_SHORT = "Structure";
                const string INARG_STATES_CONTENT_ID_SHORT = "StateMachine";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Uploads an active ticket to the ticket repository"),
                        new LangString("DE", "Lädt aktives Test-Rezept im Ticketverzeichnis hoch"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_URI_ID_SHORT),
                        new Property<string>(INARG_STRUCT_CONTENT_ID_SHORT),
                        new Property<string>(INARG_STATES_CONTENT_ID_SHORT)
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string uriString = inArgs[INARG_URI_ID_SHORT].GetValue<string>();
                        string structureContent = inArgs[INARG_STRUCT_CONTENT_ID_SHORT].GetValue<string>();
                        string statesContent = inArgs[INARG_STATES_CONTENT_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            repositoryService.UploadActiveTicket(uriString, structureContent, statesContent);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement UploadPassiveTicketOperation(string idShort)
            {
                const string INARG_TYPE1_SHELL_CONTENT = "Content";
                const string INARG_STRUCT_CONTENT_ID_SHORT = "Structure";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Uploads a passive ticket to the ticket repository"),
                        new LangString("DE", "Lädt passives Test-Rezept im Ticketverzeichnis hoch"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_TYPE1_SHELL_CONTENT),
                        new Property<string>(INARG_STRUCT_CONTENT_ID_SHORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string shellContent = inArgs[INARG_TYPE1_SHELL_CONTENT].GetValue<string>();
                        string structureContent = inArgs[INARG_STRUCT_CONTENT_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.UploadPassiveTicket(shellContent, structureContent);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement UpdateActiveTicketOperation(string idShort)
            {
                const string INARG_GUID_ID_SHORT = "TicketId";
                const string INARG_URI_ID_SHORT = "Uri";
                const string INARG_STRUCT_CONTENT_ID_SHORT = "Structure";
                const string INARG_STATES_CONTENT_ID_SHORT = "StateMachine";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Updates an active ticket based on a ticket identificator"),
                        new LangString("DE", "Aktualisiert aktives Test-Rezept nach Eingabe des entsprechenden Identifikators"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_GUID_ID_SHORT),
                        new Property<string>(INARG_URI_ID_SHORT),
                        new Property<string>(INARG_STRUCT_CONTENT_ID_SHORT),
                        new Property<string>(INARG_STATES_CONTENT_ID_SHORT)
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string guidString = inArgs[INARG_GUID_ID_SHORT].GetValue<string>();
                        string uriString = inArgs[INARG_URI_ID_SHORT].GetValue<string>();
                        string structureContent = inArgs[INARG_STRUCT_CONTENT_ID_SHORT].GetValue<string>();
                        string statesContent = inArgs[INARG_STATES_CONTENT_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.UpdateActiveTicket(guidString, uriString, structureContent, statesContent);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement UpdatePassiveTicketOperation(string idShort)
            {
                const string INARG_GUID_ID_SHORT = "TicketId";
                const string INARG_TYPE1_SHELL_CONTENT = "Content";
                const string INARG_STRUCT_CONTENT_ID_SHORT = "Structure";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Updates a passive ticket based on a ticket identificator"),
                        new LangString("DE", "Aktualisiert passives Test-Rezept nach Eingabe des entsprechenden Identifikators"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_GUID_ID_SHORT),
                        new Property<string>(INARG_TYPE1_SHELL_CONTENT),
                        new Property<string>(INARG_STRUCT_CONTENT_ID_SHORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string guidString = inArgs[INARG_GUID_ID_SHORT].GetValue<string>();
                        string shellContent = inArgs[INARG_TYPE1_SHELL_CONTENT].GetValue<string>();
                        string structureContent = inArgs[INARG_STRUCT_CONTENT_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.UpdatePassiveTicket(guidString, shellContent, structureContent);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement DeleteTicketOperation(string idShort)
            {
                const string INARG_GUID_ID_SHORT = "TicketId";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Deletes a ticket based on a ticket identificator"),
                        new LangString("DE", "Löscht ein Test-Rezept nach Eingabe des entsprechenden Ticket-Identifikators"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_GUID_ID_SHORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string guidString = inArgs[INARG_GUID_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.DeleteTicket(guidString);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement RetrieveTicketByIdOperation(string idShort)
            {
                const string INARG_GUID_ID_SHORT = "TicketId";
                const string OUTARG_DATE_CREATED = "CreatedAt";
                const string OUTARG_DATE_UPDATED = "UpdatedAt";
                const string OUTARG_AAS_TYPE = "Type";
                const string OUTARG_AAS_URI = "Uri";
                const string OUTARG_AAS_PASSIVE_SHELL = "PassiveShellContent";
                const string OUTARG_AAS_STRUCT_DESC = "StructureDescriptor";
                const string OUTARG_AAS_STATES_DESC = "StateMachineDescriptor";
                const string OUTARG_AAS_TEST_REPORT = "TestReport";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Retrieves a ticket based on a ticket identificator"),
                        new LangString("DE", "Ruft Test-Rezept nach Eingabe des entsprechenden Identifikators ab"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_GUID_ID_SHORT),
                    },
                    OutputVariables = new OperationVariableSet()
                    {
                        new Property<string>(OUTARG_DATE_CREATED),
                        new Property<string>(OUTARG_DATE_UPDATED),
                        new Property<string>(OUTARG_AAS_TYPE),
                        new Property<string>(OUTARG_AAS_URI),
                        new Property<string>(OUTARG_AAS_PASSIVE_SHELL),
                        new Property<string>(OUTARG_AAS_STRUCT_DESC),
                        new Property<string>(OUTARG_AAS_STATES_DESC),
                        new Property<string>(OUTARG_AAS_TEST_REPORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        ITestTicket? ticket = null;
                        string guidString = inArgs[INARG_GUID_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.RetrieveTicketById(guidString, ref ticket);

                            string createdAt = ticket!.CreatedAt.ToString();
                            string updatedAt = ticket.UpdatedAt == null ? string.Empty : ticket.UpdatedAt.ToString()!;
                            string type = ticket.Type.ToString();
                            string uriString = ticket.Uri == null ? string.Empty : ticket.Uri.ToString();
                            string passiveShell = ticket.PassiveShell == null ? string.Empty : repositoryService.DescriptorFactory.CreatePassiveShell(ticket.PassiveShell);
                            string structureDescriptor = repositoryService.DescriptorFactory.CreateStructureDescriptor(ticket.StructureDescriptor!.Instance);
                            string stateMachineDescriptor = ticket.StateMachineDescriptor == null ? string.Empty : repositoryService.DescriptorFactory.CreateStateMachineDescriptor(ticket.StateMachineDescriptor);
                            string testReport = ticket.TestReport == null ? string.Empty : ticket.TestReport;

                            outArgs.Add(new Property<string>(OUTARG_DATE_CREATED) { Value = createdAt });
                            outArgs.Add(new Property<string>(OUTARG_DATE_UPDATED) { Value = updatedAt });
                            outArgs.Add(new Property<string>(OUTARG_AAS_TYPE) { Value = type });
                            outArgs.Add(new Property<string>(OUTARG_AAS_URI) { Value = uriString });
                            outArgs.Add(new Property<string>(OUTARG_AAS_PASSIVE_SHELL) { Value = passiveShell });
                            outArgs.Add(new Property<string>(OUTARG_AAS_STRUCT_DESC) { Value = structureDescriptor });
                            outArgs.Add(new Property<string>(OUTARG_AAS_STATES_DESC) { Value = stateMachineDescriptor });
                            outArgs.Add(new Property<string>(OUTARG_AAS_TEST_REPORT) { Value = testReport });
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement RetrieveTicketByUriOperation(string idShort)
            {
                const string INARG_AAS_URI = "TicketUri";
                const string OUTARG_DATE_CREATED = "CreatedAt";
                const string OUTARG_DATE_UPDATED = "UpdatedAt";
                const string OUTARG_AAS_TYPE = "Type";
                const string OUTARG_AAS_ID = "TicketId";
                const string OUTARG_AAS_PASSIVE_SHELL = "ShellContent";
                const string OUTARG_AAS_STRUCT_DESC = "StructureDescriptor";
                const string OUTARG_AAS_STATES_DESC = "StateMachineDescriptor";
                const string OUTARG_AAS_TEST_REPORT = "TestReport";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Retrieves an active ticket based on the registered URI address"),
                        new LangString("DE", "Ruft aktives Test-Rezept nach Eingabe der entsprechenden URI-Adresse ab"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_AAS_URI),
                    },
                    OutputVariables = new OperationVariableSet()
                    {
                        new Property<string>(OUTARG_DATE_CREATED),
                        new Property<string>(OUTARG_DATE_UPDATED),
                        new Property<string>(OUTARG_AAS_TYPE),
                        new Property<string>(OUTARG_AAS_ID),
                        new Property<string>(OUTARG_AAS_PASSIVE_SHELL),
                        new Property<string>(OUTARG_AAS_STRUCT_DESC),
                        new Property<string>(OUTARG_AAS_STATES_DESC),
                        new Property<string>(OUTARG_AAS_TEST_REPORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        ITestTicket? ticket = null;
                        string uri = inArgs[INARG_AAS_URI].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = repositoryService.RetrieveTicketByUri(uri, ref ticket);

                            string createdAt = ticket!.CreatedAt.ToString();
                            string updatedAt = ticket.UpdatedAt == null ? string.Empty : ticket.UpdatedAt.ToString()!;
                            string type = ticket.Type.ToString();
                            string ticketId = ticket.Id.ToString();
                            string passiveShell = ticket.PassiveShell == null ? string.Empty : repositoryService.DescriptorFactory.CreatePassiveShell(ticket.PassiveShell);
                            string structureDescriptor = repositoryService.DescriptorFactory.CreateStructureDescriptor(ticket.StructureDescriptor!.Instance);
                            string stateMachineDescriptor = ticket.StateMachineDescriptor == null ? string.Empty : repositoryService.DescriptorFactory.CreateStateMachineDescriptor(ticket.StateMachineDescriptor);
                            string testReport = ticket.TestReport == null ? string.Empty : ticket.TestReport;

                            outArgs.Add(new Property<string>(OUTARG_DATE_CREATED) { Value = createdAt });
                            outArgs.Add(new Property<string>(OUTARG_DATE_UPDATED) { Value = updatedAt });
                            outArgs.Add(new Property<string>(OUTARG_AAS_TYPE) { Value = type });
                            outArgs.Add(new Property<string>(OUTARG_AAS_ID) { Value = ticketId });
                            outArgs.Add(new Property<string>(OUTARG_AAS_PASSIVE_SHELL) { Value = passiveShell });
                            outArgs.Add(new Property<string>(OUTARG_AAS_STRUCT_DESC) { Value = structureDescriptor });
                            outArgs.Add(new Property<string>(OUTARG_AAS_STATES_DESC) { Value = stateMachineDescriptor });
                            outArgs.Add(new Property<string>(OUTARG_AAS_TEST_REPORT) { Value = testReport });
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement RetrieveTicketIdsOperation(string idShort)
            {
                const string INARG_GUID_ID_SHORT = "TicketIDs";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Retrieves all ticket identificators as a comma separated string"),
                        new LangString("DE", "Ruft alle registrierten Ticket-Identifikatoren ab"),
                    },
                    OutputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_GUID_ID_SHORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            string identificators = repositoryService.RetrieveTicketIds();
                            outArgs.Add(new Property<string>(INARG_GUID_ID_SHORT) { Value = identificators });
                            message = "Successfully obtained identificators!";

                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }

            return ticketRepositorySubmodel;
        }

        /// <summary>
        /// The following method implements the submodel elements of the "TestExecution" submodel
        /// </summary>
        /// <returns>The created submodel</returns>
        private ISubmodel TestExecutionSubmodel()
        {
            ISubmodel testOrchestrationSubmodel = new Submodel("TestExecution", new Identifier("TestExecution", KeyType.IRI));

            testOrchestrationSubmodel.SubmodelElements.Add(ExecuteTestPipelineByIdOperation("ExecuteTestPipelineById"));
            testOrchestrationSubmodel.SubmodelElements.Add(ExecuteTestPipelineByUriOperation("ExecuteTestPipelineByUri"));
            testOrchestrationSubmodel.SubmodelElements.Add(ExecuteActiveTestPipelinesOperation("ExecuteActiveTestPipelines"));
            testOrchestrationSubmodel.SubmodelElements.Add(ExecutePassiveTestPipelinesOperation("ExecutePassiveTestPipelines"));
            testOrchestrationSubmodel.SubmodelElements.Add(ExecuteTestPipelinesOperation("ExecuteTestPipelines"));

            ISubmodelElement ExecuteTestPipelineByIdOperation(string idShort)
            {
                const string INARG_TICKET_ID_SHORT = "TicketId";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Executes a test pipeline for a single ticket based on the given ticket identificator"),
                        new LangString("DE", "Führt eine Testpipeline für ein einzelnes Ticket basierend auf der angegebenen Ticket-ID aus"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_TICKET_ID_SHORT),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string ticketId = inArgs[INARG_TICKET_ID_SHORT].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = orchestrationService.ExecuteTestPipelineById(ticketId);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement ExecuteTestPipelineByUriOperation(string idShort)
            {
                const string INARG_AAS_URI = "AASUri";

                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Executes a test pipeline for an active test object based on the URI address specified by the corresponding ticket"),
                        new LangString("DE", "Führt eine Testpipeline für ein aktives Testobjekt basierend auf der durch das entsprechende Ticket angegebenen URI-Adresse aus"),
                    },
                    InputVariables = new OperationVariableSet()
                    {
                        new Property<string>(INARG_AAS_URI),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string aasUri = inArgs[INARG_AAS_URI].GetValue<string>();
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = orchestrationService.ExecuteTestPipelineByUri(aasUri);
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement ExecuteActiveTestPipelinesOperation(string idShort)
            {
                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Executes a test pipeline for each active ticket in the repository"),
                        new LangString("DE", "Führt eine Testpipeline für jedes aktive Ticket im Repository aus"),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = orchestrationService.ExecuteActiveTestPipelines();
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement ExecutePassiveTestPipelinesOperation(string idShort)
            {
                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Executes a test pipeline for each passive ticket in the repository"),
                        new LangString("DE", "Führt eine Testpipeline für jedes passive Ticket im Repository aus"),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = orchestrationService.ExecutePassiveTestPipelines();
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }
            ISubmodelElement ExecuteTestPipelinesOperation(string idShort)
            {
                ISubmodelElement operation = new Operation(idShort)
                {
                    Description = new LangStringSet()
                    {
                        new LangString("EN", "Executes a test pipeline for each ticket in the repository"),
                        new LangString("DE", "Führt eine Testpipeline für jedes Ticket im Repository aus"),
                    },
                    OnMethodCalled = async (op, inArgs, inOutArgs, outArgs, cancellationToken) =>
                    {
                        string message = string.Empty;
                        bool success = true;

                        try
                        {
                            message = orchestrationService.ExecuteTestPipelines();
                        }
                        catch (Exception ex)
                        {
                            message = ex.Message;
                            success = false;
                        }

                        if (cancellationToken.IsCancellationRequested)
                            return new OperationResult(false, new Message(MessageType.Information, "Cancellation was requested"));

                        logger.Info(message);
                        return new OperationResult(success, new Message(MessageType.Information, message));
                    }
                };

                return operation;
            }

            return testOrchestrationSubmodel;
        }
    }
}

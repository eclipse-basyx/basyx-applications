using BaSyx.Models.Core.AssetAdministrationShell.Generics;

using TestDemonstrator.Enums;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket.Contracts
{
    /// <summary>
    /// The following interface defines the base characteristics of a test receipt
    /// </summary>
    public interface ITestTicket
    {
        /// <summary>
        /// GUID indeitifier of the ticket
        /// </summary>
        Guid Id { get; }

        /// <summary>
        /// Creation timestamp
        /// </summary>
        DateTime CreatedAt { get; }

        /// <summary>
        /// Actualization timestamp
        /// </summary>
        DateTime? UpdatedAt { get; }

        /// <summary>
        /// Ticket type
        /// </summary>
        TicketType Type { get; }

        /// <summary>
        /// Uri of the test object AAS (if type is active)
        /// </summary>
        Uri? Uri { get; }

        /// <summary>
        /// Test object as a passive AAS (if type is passive)
        /// </summary>
        IAssetAdministrationShell? PassiveShell { get; }

        /// <summary>
        /// Structure descriptor
        /// </summary>
        IStructureDescriptor? StructureDescriptor { get; }

        /// <summary>
        /// State machine descriptor (if type is active and state machine is implemented)
        /// </summary>
        IStateMachineDescriptor? StateMachineDescriptor { get; }

        /// <summary>
        /// Test report, loaded after the test pipeline execution
        /// </summary>
        string TestReport { get; set; }
    }
}

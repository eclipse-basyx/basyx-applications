using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using NLog;
using TestDemonstrator.Enums;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket
{
    /// <summary>
    /// The following class represents a conrete implementation of the <see cref="ITestTicket"/> interface
    /// </summary>
    public class TestTicket : ITestTicket
    {
        /// <inheritdoc/>
        private TicketType type;

        /// <inheritdoc/>
        private IStructureDescriptor? structureDescriptor;

        public TestTicket()
        {
            Id = Guid.NewGuid();
            CreatedAt = DateTime.UtcNow;
            UpdatedAt = null;
            TestReport = null;
        }

        /// <inheritdoc/>
        public Guid Id { get; set; }

        /// <inheritdoc/>
        public DateTime CreatedAt { get; set; }

        /// <inheritdoc/>
        public DateTime? UpdatedAt { get; set; }

        /// <inheritdoc/>
        public TicketType Type
        {
            get => type;
            set
            {
                type = value;
            }
        }

        /// <inheritdoc/>
        public Uri? Uri { get; set; }

        /// <inheritdoc/>
        public IAssetAdministrationShell? PassiveShell { get; set; }

        /// <inheritdoc/>
        public IStructureDescriptor? StructureDescriptor
        {
            get => structureDescriptor;
            set
            {
                if (value == null)
                    throw new ArgumentNullException();
                structureDescriptor = value;
            }
        }

        /// <inheritdoc/>
        public IStateMachineDescriptor? StateMachineDescriptor { get; set; }

        /// <inheritdoc/>
        public string? TestReport { get; set; }
    }
}

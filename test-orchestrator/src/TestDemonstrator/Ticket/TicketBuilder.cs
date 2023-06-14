
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using NLog;
using TestDemonstrator.Enums;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket
{
    /// <summary>
    /// The following class represents a concrete implementation of the <see cref="ITicketBuilder"/> interface
    /// </summary>
    public class TicketBuilder : ITicketBuilder
    {
        private static readonly ILogger logger = LogManager.GetCurrentClassLogger();

        private TestTicket ticket = new TestTicket();

        public TicketBuilder()
        {
            Reset();
        }

        public void Reset()
        {
            ticket = new TestTicket();
        }

        /// <inheritdoc/>
        public ITicketBuilder WithId(Guid id)
        {
            ticket.Id = id;
            return this;
        }

        /// <inheritdoc/>
        public ITicketBuilder WithType(TicketType type)
        {
            ticket.Type = type;
            return this;
        }

        /// <inheritdoc/>
        public ITicketBuilder WithStructureDescriptor(IStructureDescriptor descriptor)
        {
            ticket.StructureDescriptor = descriptor;
            return this;
        }

        /// <inheritdoc/>
        public ITicketBuilder WithPassiveAdministrationShell(IAssetAdministrationShell descriptor)
        {
            ticket.PassiveShell = descriptor;
            return this;
        }

        /// <inheritdoc/>
        public ITicketBuilder WithStateMachineDescriptor(IStateMachineDescriptor descriptor)
        {
            ticket.StateMachineDescriptor = descriptor;
            return this;
        }

        /// <inheritdoc/>
        public ITicketBuilder WithUrl(Uri uri)
        {
            ticket.Uri = uri;
            return this;
        }

        /// <inheritdoc/>
        public ITestTicket Build()
        {
            if (ticket.Type != TicketType.Passive &&
                ticket.Type != TicketType.Active)
            {
                logger.Warn("Ticket requires valid type!");
                throw new InvalidOperationException("Ticket requires valid type!");
            }

            if (ticket.StructureDescriptor == default)
            {
                logger.Warn("Ticket requires structure descriptor!");
                throw new InvalidOperationException("Ticket requires structure descriptor!");
            }

            if (ticket.Type == TicketType.Active)
            {
                if (ticket.Uri == null)
                {
                    logger.Warn("Active ticket requires connection string!");
                    throw new InvalidOperationException("Active ticket requires connection string!");
                }

                if (ticket.PassiveShell != null)
                {
                    logger.Warn("An active ticket cannot contain passive shell!");
                    throw new InvalidOperationException("An active ticket cannot contain passive shell!");
                }
            }

            if (ticket.Type == TicketType.Passive)
            {
                if (ticket.PassiveShell == null)
                {
                    logger.Warn("Passive ticket requires passive administration shell!");
                    throw new InvalidOperationException("Passive ticket requires passive administration shell!");
                }

                if (ticket.StateMachineDescriptor != null)
                {
                    logger.Warn("Passive ticket cannot contain state machine descriptor!");
                    throw new InvalidOperationException("Passive ticket cannot contain state machine descriptor!");
                }

                if (ticket.Uri != null)
                {
                    logger.Warn("Passive ticket cannot contain an URI!");
                    throw new InvalidOperationException("Passive ticket cannot contain an URI!");
                }
            }

            TestTicket instance = this.ticket;

            logger.Info($"Successfully created an {instance.Type.ToString().ToLower()} test ticket with ID: {instance.Id}");

            Reset();

            return instance;
        }
    }
}

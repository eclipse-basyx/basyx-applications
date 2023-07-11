using BaSyx.Models.Core.AssetAdministrationShell.Generics;

using TestDemonstrator.Enums;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket.Contracts
{
    /// <summary>
    /// The following interface is used to construct or obtain different (active or passive) test ticket instances
    /// Based on the builder pattern
    /// </summary>
    public interface ITicketBuilder
    {
        /// <summary>
        /// Specifies the ticket <see cref="ITestTicket.Id"/>, needed in order to obtain a ticket from the repository
        /// </summary>
        /// <param name="id">the ID of the ticket, already registered into the ticket repository</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithId(Guid id);

        /// <summary>
        /// Specifies the <see cref="ITestTicket.Uri"/> attribute of an active ticket
        /// </summary>
        /// <param name="uri">the URI of the ticket being created</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithUrl(Uri uri);

        /// <summary>
        /// Specifies the <see cref="ITestTicket.Type"/> attribute of a ticket
        /// </summary>
        /// <param name="type">the type of the ticket being created</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithType(TicketType type);

        /// <summary>
        /// Specifies the <see cref="ITestTicket.StructureDescriptor"/> attribute of a ticket
        /// </summary>
        /// <param name="descriptor">the structure descriptor of the ticket being created</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithStructureDescriptor(IStructureDescriptor descriptor);

        /// <summary>
        /// Specifies the <see cref="ITestTicket.StateMachineDescriptor"/> attribute of an active ticket
        /// </summary>
        /// <param name="descriptor">the state machine descriptor of the active ticket being created</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithStateMachineDescriptor(IStateMachineDescriptor descriptor);

        /// <summary>
        /// Specifies the <see cref="ITestTicket.PassiveShell"/> attribute of a passive ticket
        /// </summary>
        /// <param name="descriptor">the AAS of the test object</param>
        /// <returns>an instance of the <see cref="ITicketBuilder"/> interface</returns>
        ITicketBuilder WithPassiveAdministrationShell(IAssetAdministrationShell descriptor);

        /// <summary>
        /// Validates and constructs the <see cref="ITestTicket"/> instance
        /// </summary>
        /// <returns>A passive or an active test ticket</returns>
        /// <exception cref="InvalidOperationException">if validation error occurs</exception>
        ITestTicket Build();
    }
}

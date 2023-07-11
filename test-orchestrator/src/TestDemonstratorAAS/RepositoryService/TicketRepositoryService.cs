//Libraries
using BaSyx.Models.Core.AssetAdministrationShell.Generics;

//Core
using TestDemonstrator.Enums;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors.Contracts;
using TestDemonstrator.TicketRepository.Contracts;
using TestDemonstratorAAS.RepositoryService.Contracts;

namespace TestDemonstratorAAS.RepositoryService
{
    /// <summary>
    /// The following class serves as a concrete proof-of-concept implementation of the
    /// <see cref="ITicketRepositoryService"/> interface
    /// </summary>
    public class TicketRepositoryService : ITicketRepositoryService
    {
        private ITicketRepository ticketRepository = null!;

        private ITicketBuilder ticketBuilder = null!;

        private IDescriptorFactory descriptorFactory = null!;

        public TicketRepositoryService(ITicketRepository ticketRepository, ITicketBuilder ticketBuilder, IDescriptorFactory descriptorFactory)
        {
            this.TicketRepository = ticketRepository;

            this.TicketBuilder = ticketBuilder;

            this.DescriptorFactory = descriptorFactory;
        }

        /// <inheritdoc/>
        public ITicketRepository TicketRepository
        {
            get => ticketRepository;
            set
            {
                if (value == null)
                    throw new ArgumentNullException(nameof(value));
                ticketRepository = value;
            }
        }

        /// <inheritdoc/>
        public ITicketBuilder TicketBuilder
        {
            get => ticketBuilder;
            set
            {
                if (value == null)
                    throw new ArgumentNullException(nameof(value));
                ticketBuilder = value;
            }
        }

        /// <inheritdoc/>
        public IDescriptorFactory DescriptorFactory
        {
            get => descriptorFactory;
            set
            {
                if (value == null)
                    throw new ArgumentNullException(nameof(value));
                descriptorFactory = value;
            }
        }

        /// <inheritdoc/>
        public string UploadActiveTicket(string uriString, string structureContent, string statesContent)
        {
            if (string.IsNullOrEmpty(uriString))
                throw new ArgumentException($"Invalid Uniform Resource Identifier (URI) content detected!");

            if (string.IsNullOrEmpty(structureContent))
                throw new ArgumentException($"Invalid structure descriptor content detected!");

            // An active ticket must not provide a state machine descriptor!
            //if (string.IsNullOrEmpty(statesContent))
            //    throw new ArgumentException($"Invalid state machine descriptor content detected!");

            Uri? uri = null;
            IStructureDescriptor? structureDescriptor = null;
            IStateMachineDescriptor? stateMachineDescriptor = null;
            ITestTicket? ticket = null;
            
            try
            {
                uri = new Uri(uriString);
                structureDescriptor = descriptorFactory.CreateStructureDescriptor(structureContent);
                stateMachineDescriptor = descriptorFactory.CreateStateMachineDescriptor(statesContent);

                ticket = ticketBuilder
                    .WithType(TicketType.Active)
                    .WithUrl(uri)
                    .WithStructureDescriptor(structureDescriptor)
                    .WithStateMachineDescriptor(stateMachineDescriptor)
                    .Build();

                ticketRepository.Upload(ticket);
            }
            catch 
            {
                throw;
            }

            return $"Successfully uploaded ticket with Id: {ticket.Id}";
        }

        /// <inheritdoc/>
        public string UploadPassiveTicket(string passiveShellContent, string structureContent)
        {
            if (string.IsNullOrEmpty(passiveShellContent))
                throw new ArgumentException($"Invalid passive administration shell content detected!");

            if (string.IsNullOrEmpty(structureContent))
                throw new ArgumentException($"Invalid structure sescriptor content detected!");

            IStructureDescriptor? structureDescriptor = null;
            IAssetAdministrationShell? passiveAdministrationShell = null;
            ITestTicket? ticket = null;

            try
            {
                structureDescriptor = descriptorFactory.CreateStructureDescriptor(structureContent);
                passiveAdministrationShell = descriptorFactory.CreatePassiveShell(passiveShellContent);

                ticket = ticketBuilder
                    .WithType(TicketType.Passive)
                    .WithStructureDescriptor(structureDescriptor)
                    .WithPassiveAdministrationShell(passiveAdministrationShell)
                    .Build();

                ticketRepository.Upload(ticket);
            }
            catch
            {
                throw;
            }

            return $"Successfully uploaded ticket with Id: {ticket.Id}";
        }

        /// <inheritdoc/>
        public string UpdateActiveTicket(string guid, string uriString, string structureContent, string statesContent)
        {
            if (string.IsNullOrEmpty(guid))
                throw new ArgumentException($"Invalid ticket ID detected!");

            if (string.IsNullOrEmpty(uriString))
                throw new ArgumentException($"Invalid Uniform Resource Identifier (URI) content detected!");

            if (string.IsNullOrEmpty(structureContent))
                throw new ArgumentException($"Invalid structure descriptor content detected!");

            if (string.IsNullOrEmpty(statesContent))
                throw new ArgumentException($"Invalid state machine descriptor content detected!");

            Guid id;
            Uri? uri = null;
            IStructureDescriptor? structureDescriptor = null;
            IStateMachineDescriptor? stateMachineDescriptor = null;
            ITestTicket? ticket = null;

            try
            {
                id = new Guid(guid);
                uri = new Uri(uriString);
                structureDescriptor = descriptorFactory.CreateStructureDescriptor(structureContent);
                stateMachineDescriptor = descriptorFactory.CreateStateMachineDescriptor(statesContent);

                ticket = ticketBuilder
                    .WithId(id)
                    .WithType(TicketType.Active)
                    .WithUrl(uri)
                    .WithStructureDescriptor(structureDescriptor)
                    .WithStateMachineDescriptor(stateMachineDescriptor)
                    .Build();

                ticketRepository.Update(ticket);
            }
            catch
            {
                throw;
            }

            return $"Successfully updated ticket with Id: {ticket.Id}";
        }

        /// <inheritdoc/>
        public string UpdatePassiveTicket(string guid, string passiveShellContent, string structureContent)
        {
            if (string.IsNullOrEmpty(guid))
                throw new ArgumentException($"Invalid ticket ID detected!");

            if (string.IsNullOrEmpty(passiveShellContent))
                throw new ArgumentException($"Invalid passive administration shell content detected!");

            if (string.IsNullOrEmpty(structureContent))
                throw new ArgumentException($"Invalid structure sescriptor content detected!");

            Guid id;
            ITestTicket ticket;
            IStructureDescriptor? structureDescriptor = null;
            IAssetAdministrationShell? passiveAdministrationShell = null;

            try
            {
                id = new Guid(guid);
                passiveAdministrationShell = descriptorFactory.CreatePassiveShell(passiveShellContent);
                structureDescriptor = descriptorFactory.CreateStructureDescriptor(structureContent);

                ticket = ticketBuilder
                    .WithId(id)
                    .WithType(TicketType.Passive)
                    .WithStructureDescriptor(structureDescriptor)
                    .WithPassiveAdministrationShell(passiveAdministrationShell)
                    .Build();

                ticketRepository.Update(ticket);
            }
            catch
            {
                throw;
            }

            return $"Successfully updated ticket with Id: {ticket.Id}";
        }

        /// <inheritdoc/>
        public string DeleteTicket(string guid)
        {
            if (string.IsNullOrEmpty(guid))
                throw new ArgumentException($"Invalid ticket ID detected!");

            Guid id;

            try
            {
                id = new Guid(guid);
                ticketRepository.DeleteTicketById(id);
            }
            catch
            {
                throw;
            }

            return $"Successfully deleted ticket with Id: {guid}";
        }

        /// <inheritdoc/>
        public string RetrieveTicketById(string guid, ref ITestTicket? ticket)
        {
            if (string.IsNullOrEmpty(guid))
                throw new ArgumentException($"Invalid ticket ID detected!");

            Guid id;

            try
            {
                id = new Guid(guid);
                ticket = ticketRepository.RetrieveTicketById(id);
            }
            catch
            {
                throw;
            }

            return $"Successfully obtained ticket with Id: {guid}";
        }

        /// <inheritdoc/>
        public string RetrieveTicketByUri(string uri, ref ITestTicket? ticket)
        {
            if (string.IsNullOrEmpty(uri))
                throw new ArgumentException($"Invalid ticket Uri detected!");
            try
            {
                ticket = ticketRepository.RetrieveTicketByUrl(uri);
            }
            catch
            {
                throw;
            }


            return $"Successfully obtained ticket with Uri: {uri}";
        }

        /// <inheritdoc/>
        public string RetrieveTicketIds()
        {
            return string.Join(",", ticketRepository.Tickets.Select(t => t.Id));
        }
    }
}

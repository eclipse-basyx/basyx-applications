using AutoMapper;
using TestDemonstrator.Enums;
using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.TicketRepository.Models;

namespace TestDemonstrator.TicketRepository
{
    public class TicketMappingProfile : Profile
    {
        /// <summary>
        /// The following class specifies the mapping between 
        /// <see cref="ITestTicket"/> and <see cref="TestTicketEntity"/>
        /// </summary>
        public TicketMappingProfile()
        {
            IDescriptorFactory descriptorFactory = new DescriptorFactory();

            /// <summary>
            /// Specifying the mapping  
            /// from <see cref="ITestTicket"/> to <see cref="TestTicketEntity"/>
            /// </summary>
            CreateMap<ITestTicket, TestTicketEntity>()
                .ForMember(d => d.TicketId, mo => mo.MapFrom(s => s.Id))
                .ForMember(d => d.Type, mo => mo.MapFrom(s => s.Type))
                .ForMember(d => d.Uri, mo => mo.MapFrom(s => s.Uri!.ToString()))
                .ForMember(d => d.StructureDescriptor, mo => mo.MapFrom((s, d)
                             => descriptorFactory.CreateStructureDescriptor(s.StructureDescriptor!.Instance)))
                .ForMember(d => d.StateMachineDescriptor, mo => mo.MapFrom((s, d)
                             => s.Type == TicketType.Active && s.StateMachineDescriptor != null ?
                                descriptorFactory.CreateStateMachineDescriptor(s.StateMachineDescriptor)
                                : null))
                .ForMember(d => d.PassiveShell, mo => mo.MapFrom((s, d)
                             => s.Type == TicketType.Passive ?
                                descriptorFactory.CreatePassiveShell(s.PassiveShell!)
                                : null));

            /// <summary>
            /// Specifying the mapping  
            /// from <see cref="TestTicketEntity"/> to <see cref="ITestTicket"/>
            /// </summary>
            CreateMap<TestTicketEntity, TestTicket>()
                .ForMember(d => d.Id, mo => mo.MapFrom(s => s.TicketId))
                .ForMember(d => d.Type, mo => mo.MapFrom(s => (TicketType)Enum.Parse(typeof(TicketType), s.Type.ToString())))
                .ForMember(d => d.Uri, mo => mo.MapFrom(s => new Uri(s.Uri!)))
                .ForMember(d => d.StructureDescriptor, mo => mo.MapFrom((s, d)
                             => descriptorFactory.CreateStructureDescriptor(s.StructureDescriptor)))
                .ForMember(d => d.StateMachineDescriptor, mo => mo.MapFrom((s, d)
                             => d.Type == TicketType.Active && s.StateMachineDescriptor != null ?
                                descriptorFactory.CreateStateMachineDescriptor(s.StateMachineDescriptor)
                                : null))
                .ForMember(d => d.PassiveShell, mo => mo.MapFrom((s, d)
                             => s.Type == "Passive" ?
                                descriptorFactory.CreatePassiveShell(s.PassiveShell!)
                                : null));
        }
    }
}

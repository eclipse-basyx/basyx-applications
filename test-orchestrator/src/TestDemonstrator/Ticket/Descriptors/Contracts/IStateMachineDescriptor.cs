namespace TestDemonstrator.Ticket.Descriptors.Contracts
{
    public interface IStateMachineDescriptor : IDescriptor
    {
        public Onetimesetup Onetimesetup { get; set; }

        public Paths Paths { get; set; }

        public Onetimeteardown Onetimeteardown { get; set; }
    }
}

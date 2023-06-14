using BaSyx.Models.Core.AssetAdministrationShell.Generics;

namespace TestDemonstrator.Ticket.Descriptors.Contracts
{
    public interface IStructureDescriptor : IDescriptor
    {
        IAssetAdministrationShell Instance { get; }
    }
}

using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket.Descriptors
{
    public class StructureDescriptor : IStructureDescriptor
    {
        public StructureDescriptor(IAssetAdministrationShell aas)
        {
            Instance = aas;
        }

        public IAssetAdministrationShell Instance { get; private set; }
    }
}

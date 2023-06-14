using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket.Contracts
{
    /// <summary>
    /// The following interface is used to serialize and 
    /// deserialize the ticket descriptors
    /// </summary>
    public interface IDescriptorFactory
    {
        /// <summary>
        /// The following function is responsible for the deserialization of an AAS instances
        /// </summary>
        /// <param name="content">XML string describing an AAS</param>
        /// <returns>the deserialized AAS as XML string</returns>
        /// <exception cref="Exception">if a problem occurs</exception>
        IAssetAdministrationShell CreatePassiveShell(string content);

        /// <summary>
        /// The following function is responsible for the serialization of an AAS instances
        /// </summary>
        /// <param name="descriptor">an AAS instance implementing the <see cref="IAssetAdministrationShell"/> interface</param>
        /// <returns>the deserialized AAS as XML string</returns>
        /// <exception cref="Exception">if a problem occurs</exception>
        string CreatePassiveShell(IAssetAdministrationShell descriptor);

        /// <summary>
        /// The following function is responsible for the deserialization of an AAS instances used as structure descriptors
        /// </summary>
        /// <param name="content">XML string describing an AAS and used as a structure descriptor</param>
        /// <returns>an instance of the class <see cref="StructureDescriptor"/></returns>
        /// <exception cref="Exception">if a problem occurs</exception>
        IStructureDescriptor CreateStructureDescriptor(string content);

        /// <summary>
        /// The following function is responsible for the serialization of an AAS instances used as structure descriptors
        /// </summary>
        /// <param name="descriptor">an AAS instance implementing the <see cref="IAssetAdministrationShell"/> interface and serving as a structure descriptor</param>
        /// <returns>the deserialized structure descriptor (AAS) as XML string</returns>
        /// <exception cref="Exception">if a problem occurs</exception>
        string CreateStructureDescriptor(IAssetAdministrationShell descriptor);

        /// <summary>
        /// The following function is responsible for the deserialization of state machine descriptors
        /// </summary>
        /// <param name="content">state machine descriptor content</param>
        /// <returns>the deserialized state machine descriptor as an instance implementing the <see cref="IStateMachineDescriptor"/> interface</returns>
        /// <exception cref="InvalidOperationException">if the descriptor is invalid and can not be deserialized</exception>
        IStateMachineDescriptor CreateStateMachineDescriptor(string content);

        /// <summary>
        /// The following function is responsible for the serialization of state machine descriptors
        /// </summary>
        /// <param name="descriptor">state machine descriptor instance implementing the <see cref="IStateMachineDescriptor"/> interface</param>
        /// <returns>the serialized state machine descriptor as XML string</returns>
        /// <exception cref="Exception">if the descriptor can not be serialized</exception>
        /// <exception cref="Exception">if a problem occurs</exception>
        string CreateStateMachineDescriptor(IStateMachineDescriptor descriptor);
    }
}

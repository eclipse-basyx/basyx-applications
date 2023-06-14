using BaSyx.API.Clients;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;

using TestDemonstrator.Ticket.Contracts;

namespace TestDemonstrator.TestObjects.Contracts
{
    /// <summary>
    /// The following interface specifies the characteristics of a test object
    /// </summary>
    public interface ITestObject
    {
        /// <summary>
        /// An instance of the test receipt
        /// </summary>
        public ITestTicket Ticket { get; }

        /// <summary>
        /// An asset administration shell
        /// deep copy when testing passive test objects
        /// shallow copy when testing active test objects, that is the reason for the following client attributes
        /// </summary>
        public IAssetAdministrationShell AssetAdministrationShell { get; }

        /// <summary>
        /// An asset administration shell client
        /// Needed for the retrieval of the <see cref="AssetAdministrationShell"/> 
        /// attribute in case of an active test object
        /// </summary>
        public IAssetAdministrationShellClient Client { get; }

        /// <summary>
        /// A submodel client needed for the retrieval of submodel data
        /// </summary>
        public IAssetAdministrationShellSubmodelClient SubmodelClient { get; }

        /// <summary>
        /// A submodel repository client needed for the retrieval, creation 
        /// and destruction of many submodels
        /// </summary>
        public ISubmodelRepositoryClient SubmodelRepositoryClient { get; }

        /// <summary>
        /// An http client (not used, but still needed if other REST services must be called)
        /// </summary>
        public HttpClient HttpClient { get; }
    }
}

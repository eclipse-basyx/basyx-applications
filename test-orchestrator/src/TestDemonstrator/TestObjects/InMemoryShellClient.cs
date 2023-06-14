using BaSyx.API.Clients;
using BaSyx.Models.Communication;
using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Core.Common;
using BaSyx.Utils.ResultHandling;

namespace TestDemonstrator.TestObjects
{
    public class InMemoryShellClient : IAssetAdministrationShellClient, IAssetAdministrationShellSubmodelClient, ISubmodelRepositoryClient
    {
        private IAssetAdministrationShell shell;

        public InMemoryShellClient(IAssetAdministrationShell shell)
        {
            this.shell = shell;
        }

        public IResult<ISubmodel> CreateOrUpdateSubmodel(ISubmodel submodel)
        {
            return shell.Submodels.CreateOrUpdate(submodel.IdShort, submodel);
        }

        public IResult<ISubmodelElement> CreateOrUpdateSubmodelElement(string submodelId, string rootSeIdShortPath, ISubmodelElement submodelElement)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                var submodel = shell.Submodels[submodelId];
                return submodel.SubmodelElements.CreateOrUpdate(rootSeIdShortPath, submodelElement);
            }
            return new Result<ISubmodelElement>(false, new Message(MessageType.Error, "Submodel Element not found"));
        }

        public IResult DeleteSubmodel(string submodelId)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                return shell.Submodels.Delete(submodelId);
            }
            return new Result<ISubmodelElement>(false, new Message(MessageType.Error, "Submodel not found"));
        }

        public IResult DeleteSubmodelElement(string submodelId, string seIdShortPath)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                var submodel = shell.Submodels[submodelId];
                return submodel.SubmodelElements.Delete(seIdShortPath);
            }
            return new Result<ISubmodelElement>(false, new Message(MessageType.Error, "Submodel Element not found"));
        }

        public IResult<InvocationResponse> GetInvocationResult(string submodelId, string operationIdShortPath, string requestId)
        {
            throw new NotImplementedException();
        }

        public IResult<InvocationResponse> InvokeOperation(string submodelId, string operationIdShortPath, InvocationRequest invocationRequest)
        {
            throw new NotImplementedException();
        }

        public IResult<CallbackResponse> InvokeOperationAsync(string submodelId, string operationIdShortPath, InvocationRequest invocationRequest)
        {
            throw new NotImplementedException();
        }

        public IResult<IAssetAdministrationShell> RetrieveAssetAdministrationShell()
        {
            return new Result<IAssetAdministrationShell>(shell != null, shell!);
        }

        public IResult<ISubmodel> RetrieveSubmodel(string submodelId)
        {
            return shell.Submodels.Retrieve(submodelId);
        }

        public IResult<ISubmodelElement> RetrieveSubmodelElement(string submodelId, string seIdShortPath)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                return shell.Submodels[submodelId].SubmodelElements.Retrieve(seIdShortPath);
            }
            return new Result<ISubmodelElement>(false, new Message(MessageType.Error, "Submodel Element not found"));

        }

        public IResult<IElementContainer<ISubmodelElement>> RetrieveSubmodelElements(string submodelId)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                return shell.Submodels[submodelId].SubmodelElements.RetrieveAll();
            }
            return new Result<IElementContainer<ISubmodelElement>>(false, new Message(MessageType.Error, "Submodel Element not found"));
        }

        public IResult<IValue> RetrieveSubmodelElementValue(string submodelId, string seIdShortPath)
        {
            if (shell.Submodels.HasChild(submodelId))
            {
                var submodel = shell.Submodels[submodelId];
                if (submodel.SubmodelElements.HasChild(seIdShortPath))
                {
                    var element = shell.Submodels[submodelId].SubmodelElements[seIdShortPath];
                    var value = element.Get(element);
                    return new Result<IValue>(true, element.Get(element));
                }
                return new Result<IValue>(false, new Message(MessageType.Error, "Submodel Element not found"));

            }
            return new Result<IValue>(false, new Message(MessageType.Error, "Submodel not found"));
        }

        public IResult<IElementContainer<ISubmodel>> RetrieveSubmodels()
        {
            return shell.Submodels.RetrieveAll();
        }

        public IResult UpdateSubmodelElementValue(string submodelId, string seIdShortPath, IValue value)
        {
            var element = shell.Submodels[submodelId].SubmodelElements[seIdShortPath];
            element.Set(element, value);
            return new Result(true);
        }
    }
}

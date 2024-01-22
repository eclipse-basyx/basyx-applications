package org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.aggregator.aas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AasEnv;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.aas.restapi.api.IAASAPI;
import org.eclipse.basyx.aas.restapi.api.IAASAPIFactory;
import org.eclipse.basyx.extensions.shared.authorization.internal.NotAuthorizedException;
import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregator;
import org.eclipse.basyx.submodel.aggregator.api.ISubmodelAggregatorFactory;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.submodel.restapi.api.ISubmodelAPI;
import org.eclipse.basyx.vab.exception.FeatureNotImplementedException;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.aas.DelegationAasAPI;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.component.api.submodel.DelegationSubmodelAPI;
import org.eclipse.digitaltwin.basyx.dataintegrator.aas.core.runner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegationAasAggregator implements IAASAggregator {
	
	private static Logger logger = LoggerFactory.getLogger(DelegationAasAggregator.class);
	
	protected Map<String, MultiSubmodelProvider> aasProviderMap = new HashMap<>();
	private AasEnv aasEnv;
	
	protected ISubmodelAggregatorFactory submodelAggregatorFactory;
	protected IAASAPIFactory aasApiProvider;
	private IAASRegistry registry;
	
	private JobRunner jobRunner;
	
	public DelegationAasAggregator(JobRunner jobRunner) {
		super();
		this.jobRunner = jobRunner;
//		init();
	}
	
	public DelegationAasAggregator(JobRunner jobRunner, ISubmodelAggregatorFactory submodelAggregatorFactory, IAASAPIFactory aasApiFactory) {
		super();
		this.jobRunner = jobRunner;
		this.submodelAggregatorFactory = submodelAggregatorFactory;
		this.aasApiProvider = aasApiFactory;
//		init();
	}
	
	public DelegationAasAggregator(JobRunner jobRunner, ISubmodelAggregatorFactory submodelAggregatorFactory, IAASAPIFactory aasApiFactory, IAASRegistry aasRegistry) {
		super();
		this.jobRunner = jobRunner;
		this.submodelAggregatorFactory = submodelAggregatorFactory;
		this.aasApiProvider = aasApiFactory;
		this.registry = aasRegistry;
//		init();
	}
	
	public JobRunner getJobRunner() {
		return jobRunner;
	}
	
	private void init(String param) {
		this.aasEnv = this.jobRunner.getAasEnv(param);
		
		List<AssetAdministrationShell> administrationShells = this.aasEnv.getAssetAdministrationShells().stream().map(AssetAdministrationShell.class::cast).collect(Collectors.toList());
		
		for (AssetAdministrationShell aas : administrationShells) {
			String aasId = aas.getIdentification().getId();
			logger.info("Adding AAS from legacy system: " + aasId);
			DelegationAasAPI delegationAasAPI = new DelegationAasAPI(this.jobRunner, aasId);
			delegationAasAPI.setAAS(aas);
			
			MultiSubmodelProvider provider = createMultiSubmodelProvider(delegationAasAPI);
			addSubmodelsFromLegacySystem(provider, aas);
			aasProviderMap.put(aas.getIdentification().getId(), provider);
		}
	}

	private MultiSubmodelProvider createMultiSubmodelProvider(IAASAPI aasApi) {
		AASModelProvider aasProvider = new AASModelProvider(aasApi);
		IConnectorFactory connProvider = new HTTPConnectorFactory();

		ISubmodelAggregator usedAggregator = getSubmodelAggregatorInstance();

		return new MultiSubmodelProvider(aasProvider, registry, connProvider, aasApiProvider, usedAggregator);
	}
	
	private ISubmodelAggregator getSubmodelAggregatorInstance() {
		return submodelAggregatorFactory.create();
	}
	
	private void addSubmodelsFromLegacySystem(MultiSubmodelProvider provider, AssetAdministrationShell aas) {
		// Get ids and idShorts from aas
		Collection<IReference> submodelRefs = aas.getSubmodelReferences();
		List<String> smIds = new ArrayList<>();
		List<String> smIdShorts = new ArrayList<>();
		for (IReference ref : submodelRefs) {
			List<IKey> keys = ref.getKeys();
			IKey lastKey = keys.get(keys.size() - 1);
			if (lastKey.getIdType() == KeyType.IDSHORT) {
				smIdShorts.add(lastKey.getValue());
			} else {
				smIds.add(lastKey.getValue());
			}
		}

		// Add submodel ids by id shorts
		for (String idShort : smIdShorts) {
			String id = getSubmodelId(idShort);
			if (id != null) {
				smIds.add(id);
			}
		}

		// Create a provider for each submodel
		for (String id : smIds) {
			logger.info("Adding Submodel from legacy system: " + id);
			addSubmodelProvidersById(getSubmodelById(id), provider);
		}
	}
	
	private String getSubmodelId(String idShort) {
		Optional<ISubmodel> sm = this.aasEnv.getSubmodels().stream().filter(submodel -> submodel.getIdShort().equals(idShort)).findAny();
		
		if (sm.isEmpty())
			return null;
		
		return sm.get().getIdentification().getId();
	}
	
	private ISubmodel getSubmodelById(String id) {
		Optional<ISubmodel> sm = this.aasEnv.getSubmodels().stream().filter(submodel -> submodel.getIdentification().getId().equals(id)).findAny();
		
		if (sm.isEmpty())
			return null;
		
		return sm.get();
	}
	
	private void addSubmodelProvidersById(ISubmodel submodel, MultiSubmodelProvider provider) {
		ISubmodelAPI smApi = new DelegationSubmodelAPI(this.jobRunner, (Submodel) submodel);
		
		SubmodelProvider smProvider = new SubmodelProvider(smApi);
		provider.addSubmodel(smProvider);
	}

	@Override
	public Collection<IAssetAdministrationShell> getAASList() {
		return aasProviderMap.values().stream().map(p -> {
			try {
				return p.getValue("/aas");
			} catch (NotAuthorizedException e) {
				return null;
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new RuntimeException();
			}
		}).filter(Objects::nonNull).map(m -> {
			AssetAdministrationShell aas = new AssetAdministrationShell();
			aas.putAll((Map<? extends String, ? extends Object>) m);
			return aas;
		}).collect(Collectors.toList());
	}

	@Override
	public IAssetAdministrationShell getAAS(IIdentifier aasId) throws ResourceNotFoundException {
		IModelProvider aasProvider = getAASProvider(aasId);

		// get all Elements from provider
		@SuppressWarnings("unchecked")
		Map<String, Object> aasMap = (Map<String, Object>) aasProvider.getValue("/aas");
		
		aasProviderMap.clear();
		
		return AssetAdministrationShell.createAsFacade(aasMap);
	}

	@Override
	public IModelProvider getAASProvider(IIdentifier aasId) throws ResourceNotFoundException {
		init(aasId.getId());
		MultiSubmodelProvider provider = aasProviderMap.get(aasId.getId());

		if (provider == null) {
			throw new ResourceNotFoundException("AAS with Id " + aasId.getId() + " does not exist");
		}

		return provider;
	}

	@Override
	public void createAAS(AssetAdministrationShell aas) {
		throw new FeatureNotImplementedException();
	}

	@Override
	public void updateAAS(AssetAdministrationShell aas) throws ResourceNotFoundException {
		throw new FeatureNotImplementedException();
	}

	@Override
	public void deleteAAS(IIdentifier aasId) {
		throw new FeatureNotImplementedException();
	}

}

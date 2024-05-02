import { defineComponent } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import md5 from 'md5';

import { useAASStore } from '@/store/AASDataStore';
import { useNavigationStore } from '@/store/NavigationStore';

import RequestHandling from '@/mixins/RequestHandling';
import { element } from 'three/examples/jsm/nodes/shadernode/ShaderNode';

export default defineComponent({
    name: 'SubmodelElementHandling',
    mixins: [RequestHandling],

    setup() {
        const aasStore = useAASStore();
        const navigationStore = useNavigationStore();

        return {
            aasStore, // AASStore Object
            navigationStore, // NavigationStore Object
        }
    },

    computed: {
        // get AAS Discovery URL from Store
        aasDiscoveryURL() {
            return this.navigationStore.getAASDiscoveryURL;
        },

        // get AAS Registry URL from Store
        aasRegistryURL() {
            return this.navigationStore.getAASRegistryURL;
        },

        // Get the Submodel Repository URL from the Store
        submodelRepoURL() {
            return this.navigationStore.getSubmodelRepoURL;
        },

        // Get the Concept Description Repository URL from the Store
        conceptDescriptionRepoURL() {
            return this.navigationStore.getConceptDescriptionRepoURL;
        },

        // Get the Selected AAS from the Store
        selectedAAS() {
            return this.aasStore.getSelectedAAS;
        },
    },

    methods: {
        // converts AAS identification to UTF8 BASE64 encoded URL
        URLEncode(aasId: string) {
            const base64Id = btoa(unescape(encodeURIComponent(aasId)));
            const urlSafeBase64Id = base64Id.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
            return urlSafeBase64Id;
        },

        // generate a unique ID (UUID)
        UUID() {
            return uuidv4();
        },

        // generate a unique ID (UUID) from a given string
        generateUUIDFromString(str: any): string {
            // create md5 hash from string
            const hash = md5(str);
            // create UUID from hash
            const guid = hash.substring(0, 8) + '-' + hash.substring(8, 12) + '-' + hash.substring(12, 16) + '-' + hash.substring(16, 20) + '-' + hash.substring(20, 32);
            return guid;
        },

        // convert date element to digits
        padTo2Digits(num: number) {
            return num.toString().padStart(2, '0');
        },

        // convert js date object to string
        formatDate(date: Date) {
            return (
                [
                    date.getFullYear(),
                    this.padTo2Digits(date.getMonth() + 1),
                    this.padTo2Digits(date.getDate()),
                ].join('-') +
                ' ' +
                [
                    this.padTo2Digits(date.getHours()),
                    this.padTo2Digits(date.getMinutes()),
                    this.padTo2Digits(date.getSeconds()),
                ].join(':')
            );
        },

        // Function to capitalize the first letter of a string
        capitalizeFirstLetter(string: string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        },

        // Function to check if the SemanticID of a SubmodelElement matches the given SemanticID
        checkSemanticId(submodelElementData: any, semanticId: string): boolean {
            let result = false;
            submodelElementData.semanticId.keys.forEach((key: any) => {
                if (key.value === semanticId) {
                    result = true;
                }
            });
            return result;
        },

        // Function to check if the valueType is a number
        isNumber(valueType: any) {
            if (!valueType) return false;
            // List of all number types
            let numberTypes = [
                'double',
                'float',
                'integer',
                'int',
                'nonNegativeInteger',
                'positiveInteger',
                'unsignedLong',
                'unsignedInt',
                'unsignedShort',
                'unsignedByte',
                'nonPositiveInteger',
                'negativeInteger',
                'long',
                'short',
                'decimal',
                'byte'
            ];
            // strip xs: from the property if it exists
            if (valueType.includes('xs:')) {
                valueType = valueType.replace('xs:', '');
            }
            // check if the property is a number
            if (numberTypes.includes(valueType)) {
                return true;
            } else {
                return false;
            }
        },

        // Function to download a JSON File
        downloadJson(obj: any, fileName: string) {
            const jsonStr = JSON.stringify(obj, null, 4);
            const blob = new Blob([jsonStr], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        },

        // Function to download a binary File
        downloadFile(filename: string, fileContent: Blob, contentType: string) {
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(fileContent);
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        },

        // Function to check if the referenced Element exists
        async checkReference(referenceValue: Array<any>): Promise<{ success: boolean, aas?: object, submodel?: object }> {
            // console.log('Reference Value: ', referenceValue);
            // check if aasRegistryURL includes "/shell-descriptors" and add id if not (backward compatibility)
            if (!this.aasRegistryURL.includes('/shell-descriptors')) {
                this.aasRegistryURL += '/shell-descriptors';
            }
            let path = this.aasRegistryURL;
            let context = 'retrieving AAS Data';
            let disableMessage = false;
            try {
                const response = await this.getRequest(path, context, disableMessage);
                // console.log('Response: ', response);
                if (response.success && response.data.result && response.data.result.length > 0) {
                    let aasList = response.data.result;
                    if (referenceValue[0].type == 'AssetAdministrationShell') {
                        return await this.checkReferenceAAS(aasList, referenceValue);
                    }
                    if (referenceValue[0].type == 'Submodel') {
                        return await this.checkReferenceSubmodel(aasList, referenceValue);
                    }
                }
                return { success: false, aas: {}, submodel: {} };
            } catch (error) {
                // handle error
                return { success: false, aas: {}, submodel: {} };
            }
        },

        // Function to check if the referenced AAS exists
        async checkReferenceAAS(aasList: Array<any>, referenceValue: Array<any>): Promise<{ success: boolean, aas?: object, submodel?: object }> {
            try {
                aasList.forEach((aas: any) => {
                    if (aas.id == referenceValue[0].value) {
                        // console.log('AAS found. AAS: ', { success: true, aas: aas, submodel: {} });
                        throw { success: true, aas: aas, submodel: {} };
                    }
                });
            } catch (result: any) {
                if (result.success) {
                    return result;
                } else {
                    throw result; // re-throw if it's an actual error
                }
            }
            return { success: false, aas: {}, submodel: {} };
        },

        // Function to check if the referenced Submodel (+ SubmodelElement) exists
        async checkReferenceSubmodel(aasList: Array<any>, referenceValue: Array<any>): Promise<{ success: boolean, aas?: object, submodel?: object }> {
            const promises = aasList.map(async (aas: any) => {
                let path = aas.endpoints[0].protocolInformation.href + '/submodel-refs';
                let context = 'retrieving Submodel References';
                let disableMessage = false;

                const response = await this.getRequest(path, context, disableMessage);
                if (response.success) {
                    const submodelList = response.data.result;
                    const foundSubmodel = submodelList.find((submodel: any) => submodel.keys[0].value == referenceValue[0].value);
                    if (foundSubmodel) {
                        return { success: true, aas: aas, submodel: foundSubmodel };
                    }
                }
                return null; // null signifies that this particular iteration didn't find what it was looking for
            });

            const results = await Promise.all(promises);
            const foundResult = results.find(result => result !== null);

            if (foundResult) {
                return foundResult; // One of the iterations was successful
            } else {
                return { success: false, aas: {}, submodel: {} }; // None of the iterations were successful
            }
        },

        // Function to jump to a referenced Element
        jumpToReferencedElement(referencedAAS: any, referenceValue: Array<any>, referencedSubmodel?: any) {
            // console.log('jumpToReferencedElement. AAS: ', referencedAAS, 'Submodel: ', referencedSubmodel);
            let endpoint = referencedAAS.endpoints[0].protocolInformation.href;
            if (referencedSubmodel && Object.keys(referencedSubmodel).length > 0) { // if the referenced Element is a Submodel or SubmodelElement
                this.jumpToSubmodelElement(referencedSubmodel, referenceValue, referencedAAS, endpoint);
            } else { // if the referenced Element is an AAS
                this.jumpToAAS(referencedAAS, endpoint);
            }
        },

        jumpToSubmodelElement(referencedSubmodel: any, referenceValue: Array<any>, referencedAAS: any, endpoint: string) {
            let path = this.submodelRepoURL + '/' + this.URLEncode(referencedSubmodel.keys[0].value);
            if (referenceValue.length > 1) { // this is the layer directly under the Submodel
                path += '/submodel-elements/' + referenceValue[1].value;
            }
            let promise; // Promise to wait for the SubmodelElementList to be requested (if it exists)
            if (referenceValue.length > 2) { // this is the layer under either a SubmodelElementCollection or SubmodelElementList
                promise = new Promise<void>((resolve, reject) => {
                    referenceValue.forEach((SubmodelElement: any, index: number) => {
                        if (index > 1) {
                            // check if the type of the SubmodelElement with index - 1 is a SubmodelElementList
                            if (referenceValue[index - 1].type == 'SubmodelElementList') {
                                // console.log('SubmodelElementList: ', this.referenceValue[index - 1])
                                // check in which position of the list the element is (list needs to be requested to get the position)
                                let listPath = path
                                let context = 'retrieving SubmodelElementList';
                                let disableMessage = false;
                                this.getRequest(listPath, context, disableMessage).then((response: any) => {
                                    if (response.success) { // execute if the Request was successful
                                        let list = response.data;
                                        list.value.forEach((element: any, i: number) => {
                                            if (element.idShort == SubmodelElement.value) {
                                                path += encodeURIComponent('[') + i + encodeURIComponent(']');
                                            }
                                        });
                                        resolve();
                                    }
                                }).catch((error: any) => {
                                    // console.error('Error with getRequest:', error);
                                    reject(error);
                                });
                            } else {
                                path += '.' + SubmodelElement.value;
                            }
                        }
                    });
                    if (referenceValue.every((SubmodelElement: any, index: number) => index <= 1 || referenceValue[index - 1].type != 'SubmodelElementList')) {
                        resolve();  // Resolve immediately if none of the elements are SubmodelElementList
                    }
                });
            } else {
                promise = Promise.resolve();
            }

            promise.then(() => {
                // set the AAS Endpoint and SubmodelElement path in the aas and path query parameters using the router
                this.$router.push({ query: { aas: endpoint, path: path } });
                // dispatch the AAS set by the ReferenceElement to the store
                this.aasStore.dispatchSelectedAAS(referencedAAS);
                // Request the referenced SubmodelElement
                let elementPath = path;
                let context = 'retrieving SubmodelElement';
                let disableMessage = true;
                this.getRequest(elementPath, context, disableMessage).then((response: any) => {
                    if (response.success) { // execute if the Request was successful
                        response.data.timestamp = this.formatDate(new Date()); // add timestamp to the SubmodelElement Data
                        response.data.path = path; // add the path to the SubmodelElement Data
                        response.data.isActive = true; // add the isActive Property to the SubmodelElement Data
                        // console.log('SubmodelElement Data: ', response.data)
                        // dispatch the SubmodelElementPath set by the URL to the store
                        this.aasStore.dispatchNode(response.data); // set the updatedNode in the AASStore
                        this.aasStore.dispatchInitTreeByReferenceElement(true); // set the initTreeByReferenceElement in the AASStore to true to init + expand the Treeview on the referenced Element
                    } else { // execute if the Request failed
                        if (Object.keys(response.data).length == 0) {
                            // don't copy the static SubmodelElement Data if no Node is selected or Node is invalid
                            this.navigationStore.dispatchSnackbar({ status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'No valid SubmodelElement under the given Path' }); // Show Error Snackbar
                            return;
                        }
                        this.aasStore.dispatchNode({});
                    }
                });
            }).catch(error => {
                console.error('Error:', error);
            });
        },

        jumpToAAS(aas: any, endpoint: string) {
            // set the AAS Endpoint in the aas query parameter using the router
            this.$router.push({ query: { aas: endpoint } });
            // dispatch the AAS set by the ReferenceElement to the store
            // console.log('AAS:', aas, 'Endpoint:', endpoint);
            this.aasStore.dispatchSelectedAAS(aas);
            this.aasStore.dispatchNode({});
        },

        // Function to check if the assetId can be found in the AAS Discovery Service (and if it exists in the AAS Registry)
        async checkAssetId(assetId: string): Promise<{ success: boolean, aas?: object, submodel?: object }> {
            const failResponse = { success: false, aas: {}, submodel: {} }; // Define once for reuse
            // check if aasDiscoveryURL includes "/lookup/shells" and add id if not (backward compatibility)
            if (!this.aasDiscoveryURL.includes('/lookup/shells')) {
                this.aasDiscoveryURL += '/lookup/shells';
            }
            const path = `${this.aasDiscoveryURL}?assetIds=${this.URLEncode(assetId)}`; // Use template literal and encodeURIComponent
            const context = 'retrieving AASID by AssetID';
            const disableMessage = true;
            try {
                const discoveryResponse = await this.getRequest(path, context, disableMessage);
                // console.log('Discovery Response:', discoveryResponse);
                if (discoveryResponse.success && discoveryResponse.data.result?.length > 0) {
                    const aasIds = discoveryResponse.data.result;
                    // take the first aasId from the list and check if it exists in the AAS Registry
                    const aasId = aasIds[0];
                    // console.log('AAS ID:', aasId);
                    // check if aasRegistryURL includes "/shell-descriptors" and add id if not (backward compatibility)
                    if (!this.aasRegistryURL.includes('/shell-descriptors')) {
                        this.aasRegistryURL += '/shell-descriptors';
                    }
                    const registryPath = `${this.aasRegistryURL}/${this.URLEncode(aasId)}`;
                    const registryContext = 'retrieving AAS Data';
                    try {
                        const aasRegistryResponse = await this.getRequest(registryPath, registryContext, disableMessage);
                        if (aasRegistryResponse.success) {
                            const aas = aasRegistryResponse.data;
                            // console.log('AAS:', aas);
                            return { success: true, aas: aas, submodel: {} };
                        }
                        return failResponse;
                    } catch {
                        return failResponse;
                    }
                }
                return failResponse;
            } catch {
                return failResponse;
            }
        },

        // Get the ConceptDescriptions for the SubmodelElement from the ConceptDescription Repository
        getConceptDescription(SelectedNode: any) {
            let conceptDescriptionRepoURL = '';
            if (this.conceptDescriptionRepoURL && this.conceptDescriptionRepoURL != '') {
                conceptDescriptionRepoURL = this.conceptDescriptionRepoURL;
            } else {
                return Promise.resolve({}); // Return an empty object wrapped in a resolved promise
            }
            // return if no SemanticID is available
            if (!SelectedNode.semanticId || !SelectedNode.semanticId.keys || SelectedNode.semanticId.keys.length == 0) {
                return Promise.resolve({});
            }
            let path = conceptDescriptionRepoURL + "/" + this.URLEncode(SelectedNode.semanticId.keys[0].value);
            let context = 'retrieving ConceptDescription';
            let disableMessage = true;

            // Return the promise from getRequest
            return this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // console.log('ConceptDescription Data: ', response.data);
                    let conceptDescription = response.data;
                    conceptDescription.path = path;
                    return conceptDescription;
                } else {
                    return {};
                }
            });
        },

        // calculate the pathes of the SubmodelElements in a provided Submodel/SubmodelElement
        calculateSubmodelElementPathes(parent: any, startPath: string): any {
            // console.log('Parent: ', parent, 'StartPath: ', startPath);
            parent.path = startPath;
            parent.id = this.UUID();
            // get the conceptDescription for the SubmodelElement
            this.getConceptDescription(parent).then((response: any) => {
                if (response) {
                    parent.conceptDescription = response;
                }
            });
            // check for children
            if (parent.submodelElements && parent.submodelElements.length > 0) { // check for SubmodelElements
                parent.submodelElements.forEach((element: any) => {
                    element = this.calculateSubmodelElementPathes(element, startPath + '/submodel-elements/' + element.idShort);
                });
            } else if (parent.value && Array.isArray(parent.value) && parent.value.length > 0 && parent.modelType == 'SubmodelElementCollection') { // check for Values (SubmodelElementCollections or SubmodelElementLists)
                parent.value.forEach((element: any) => {
                    element = this.calculateSubmodelElementPathes(element, startPath + '.' + element.idShort);
                });
            } else if (parent.value && Array.isArray(parent.value) && parent.value.length > 0 && parent.modelType == 'SubmodelElementList') { // check for Values (SubmodelElementCollections or SubmodelElementLists)
                parent.value.forEach((element: any, index: number) => {
                    element = this.calculateSubmodelElementPathes(element, startPath + encodeURIComponent('[') + index + encodeURIComponent(']'));
                });
            } else if (parent.statements && Array.isArray(parent.statements) && parent.statements.length > 0 && parent.modelType == 'Entity') { // check for Statements (Entities)
                parent.value.forEach((element: any) => {
                    element = this.calculateSubmodelElementPathes(element, startPath + '.' + element.idShort);
                });
            }
            return parent;
        },

        // Function to calculate the local path (used for files)
        getLocalPath(path: string, selectedNode: any): string {
            if (!path) return '';
            // check if Link starts with '/'
            if (path.startsWith('/')) {
                path = selectedNode.path + '/attachment';
            }
            return path;
        }
    },
})
interface state {
    aasObject: object,
    loadingState: boolean,
    updatedNode: object,
    selectedNode: object,
    realTimeObject: object,
    initTreeByReferenceElement: boolean,
};

const state: state = {
    aasObject: {}, // holds the AAS object for the currently selected AAS
    loadingState: false, // loading state of the AAS Treeview Component
    updatedNode: {}, // holds the most recently updated Node in the AAS Treeview Component
    selectedNode: {}, // holds the currently selected Node in the AAS Treeview Component
    realTimeObject: {}, // holds the newest synced SubmodelElement (for the ComponentViasualization)
    initTreeByReferenceElement: false, // holds the state if the AAS Treeview Component should be initialized because the Jump-Button was clicked on a ReferenceElement
};

const getters = {
    // returns the selected AAS
    getSelectedAAS(state: state) {
        return state.aasObject;
    },
    // returns the loading state of the AAS Treeview Component
    getLoadingState(state: state) {
        return state.loadingState;
    },
    // returns the most recently updated Node in the AAS Treeview Component
    getUpdatedNode(state: state) {
        return state.updatedNode;
    },
    // returns the currently selected Node in the AAS Treeview Component
    getSelectedNode(state: state) {
        return state.selectedNode;
    },
    // returns the newest synced SubmodelElement (for the ComponentViasualization)
    getRealTimeObject(state: state) {
        return state.realTimeObject;
    },
    // returns the state if the AAS Treeview Component should be initialized because the Jump-Button was clicked on a ReferenceElement
    getInitTreeByReferenceElement(state: state) {
        return state.initTreeByReferenceElement;
    },
};

const actions = {
    // dispatches Action to set the selected AAS
    dispatchSelectedAAS({ commit }: { commit: Function }, aasData: object) {
        // console.log('dispatchSelectedAAS: ', aasData);
        commit('setSelectedAAS', aasData);
        commit('setSelectedNode', {});
    },
    // dispatches Action to set the loading state of the AAS Treeview Component
    dispatchLoadingState({ commit }: { commit: Function }, loadingState: boolean) {
        // console.log('dispatchLoadingState: ', loadingState);
        commit('setLoadingState', loadingState);
    },
    // dispatches Action to toggle a Node in the AAS Treeview Component
    dispatchNode({ commit }: { commit: Function }, node: any) {
        // console.log('dispatchNode: ', node);
        commit('setUpdatedNode', node);
        if(!node.isActive) node = {}; 
        commit('setSelectedNode', node);
    },
    // dispatches Action to set the newest synced SubmodelElement (for the ComponentViasualization)
    dispatchRealTimeObject({ commit }: { commit: Function }, realTimeObject: any) {
        // console.log('dispatchRealTimeObject: ', realTimeObject);
        commit('setRealTimeObject', realTimeObject);
    },
    // dispatches Action to set the state if the AAS Treeview Component should be initialized because the Jump-Button was clicked on a ReferenceElement
    dispatchInitTreeByReferenceElement({ commit }: { commit: Function }, initTreeByReferenceElement: boolean) {
        // console.log('dispatchInitTreeByReferenceElement: ', initTreeByReferenceElement);
        commit('setInitTreeByReferenceElement', initTreeByReferenceElement);
    },
};

const mutations = {
    // commit Mutation for the selected AAS to set the State
    setSelectedAAS(state: state, aasData: object ) {
        state.aasObject = aasData;
    },
    // commit Mutation for the loading state of the AAS Treeview Component to set the State
    setLoadingState(state: state, loadingState: boolean) {
        state.loadingState = loadingState;
    },
    // commit Mutation for the updated Node in the AAS Treeview Component to set the State
    setUpdatedNode(state: state, node: object) {
        state.updatedNode = node;
    },
    // commit Mutation for the selected Node in the AAS Treeview Component to set the State
    setSelectedNode(state: state, node: object) {
        state.selectedNode = node;
    },
    // commit Mutation for the newest synced SubmodelElement (for the ComponentViasualization) to set the State
    setRealTimeObject(state: state, realTimeObject: any) {
        state.realTimeObject = realTimeObject;
    },
    // commit Mutation for the state if the AAS Treeview Component should be initialized because the Jump-Button was clicked on a ReferenceElement to set the State
    setInitTreeByReferenceElement(state: state, initTreeByReferenceElement: boolean) {
        state.initTreeByReferenceElement = initTreeByReferenceElement;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};
interface state {
    WidgetFeatureActive: boolean,
    updateWidget: boolean,
};

const state: state = {
    WidgetFeatureActive: false, // holds the disable state of the widget
    updateWidget: false, // holds the update state of the widget
};

const getters = {
    // returns the disable state of the widget
    getWidgetFeatureActive(state: state) {
        return state.WidgetFeatureActive;
    },
    // returns the update state of the widget
    getUpdateWidget(state: state) {
        return state.updateWidget;
    },
};

const actions = {
    // dispatches Action to activate/deactivate the widget feature
    dispatchWidgetFeatureActive({ commit }: { commit: Function }, activeState: boolean) {
        // console.log('dispatchWidgetFeatureActive: ', activeState);
        commit('setWidgetFeatureActive', activeState);
    },
    // dispatches Action to update the widget
    dispatchUpdateWidget({ commit }: { commit: Function }, updateState: boolean) {
        // console.log('dispatchUpdateWidget: ', updateState);
        commit('setUpdateWidget', updateState);
    },
};

const mutations = {
    // commit Mutation to activate/deactivate the widget feature
    setWidgetFeatureActive(state: state, activeState: boolean) {
        state.WidgetFeatureActive = activeState;
    },
    // commit Mutation to update the widget
    setUpdateWidget(state: state, updateState: boolean) {
        state.updateWidget = updateState;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};
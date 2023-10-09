import { defineStore } from 'pinia';

export const useWidgetsStore = defineStore({
    id: 'widgetsStore',
    state: () => ({
        WidgetFeatureActive: false, // holds the disable state of the widget
        updateWidget: false, // holds the update state of the widget
    }),
    getters: {
        getWidgetFeatureActive(state) {
            return state.WidgetFeatureActive;
        },
        getUpdateWidget(state) {
            return state.updateWidget;
        },
    },
    actions: {
        dispatchWidgetFeatureActive(activeState: boolean) {
            this.setWidgetFeatureActive(activeState);
        },
        dispatchUpdateWidget(updateState: boolean) {
            this.setUpdateWidget(updateState);
        },
        setWidgetFeatureActive(activeState: boolean) {
            this.WidgetFeatureActive = activeState;
        },
        setUpdateWidget(updateState: boolean) {
            this.updateWidget = updateState;
        },
    },
});

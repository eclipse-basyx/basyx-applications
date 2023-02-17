import { defineComponent } from 'vue';
import { useStore } from 'vuex';

export default defineComponent({
    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
        }
    },
    methods: {
        // generate a unique ID (UUID)
        UUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
    },
})
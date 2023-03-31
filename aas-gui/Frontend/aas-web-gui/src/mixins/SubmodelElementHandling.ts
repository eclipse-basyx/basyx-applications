import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import { v4 as uuidv4 } from 'uuid';
import md5 from 'md5';

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
    },
})
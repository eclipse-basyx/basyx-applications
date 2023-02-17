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
        // Function to send get Request which returns a Promise
        getRequest(path: string, context: string, disableMessage: boolean): any {
            return fetch(path, { method: 'GET' })
                .then(response => {
                    // Check if the Server responded with content
                    if (response.headers.get('Content-Type')?.split(';')[0] === 'application/json' && response.headers.get('Content-Length') !== '0') {
                        return response.json(); // Return the response as JSON
                    } else {
                        return; // Return without content
                    }
                })
                .then(data => {
                    // Check if the Server responded with an error
                    if (data && data.hasOwnProperty('success') && data.success === false) { // If the Server responded with an error
                        if(!disableMessage) this.errorHandler(data, context); // Call the error handler
                        return { success: false };
                    } else { // If the Server responded with a success
                        return { success: true, data: data };
                    }
                })
                .catch(error => { // Catch any errors
                    console.error('Error: ', error); // Log the error
                    if (!disableMessage) this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
                    return { success: false };
                })
        },

        // Function to send post Request which returns a Promise
        postRequest(path: string, body: any, headers: any, context: string, disableMessage: boolean): any {
            return fetch(path, { method: 'POST', body: body, headers: headers })
                .then(response => {
                    // Check if the Server responded with content
                    if (response.headers.get('Content-Type')?.split(';')[0] === 'application/json' && response.headers.get('Content-Length') !== '0') {
                        return response.json(); // Return the response as JSON
                    } else {
                        return; // Return without content
                    }
                })
                .then(data => {
                    // Check if the Server responded with an error
                    if (data && data.hasOwnProperty('success') && data.success === false) { // If the Server responded with an error
                        if (!disableMessage) this.errorHandler(data, context); // Call the error handler
                        return { success: false };
                    } else { // If the Server responded with a success
                        return { success: true, data: data };
                    }
                })
                .catch(error => { // Catch any errors
                    console.error('Error: ', error); // Log the error
                    if (!disableMessage) this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
                    return { success: false };
                })
        },

        // Function to send put Request which returns a Promise
        putRequest(path: string, body: any, headers: any, context: string, disableMessage: boolean): any {
            return fetch(path, { method: 'PUT', body: body, headers: headers })
                .then(response => {
                    // Check if the Server responded with content
                    if (response.headers.get('Content-Type')?.split(';')[0] === 'application/json' && response.headers.get('Content-Length') !== '0') {
                        return response.json(); // Return the response as JSON
                    } else {
                        return; // Return without content
                    }
                })
                .then(data => {
                    // Check if the Server responded with an error
                    if (data && data.hasOwnProperty('success') && data.success === false) { // If the Server responded with an error
                        if (!disableMessage) this.errorHandler(data, context); // Call the error handler
                        return { success: false };
                    } else { // If the Server responded with a success
                        return { success: true };
                    }
                })
                .catch(error => { // Catch any errors
                    console.error('Error: ', error); // Log the error
                    if (!disableMessage) this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
                    return { success: false };
                })
        },

        // Function to send delete Request which returns a Promise
        deleteRequest(path: string, context: string, disableMessage: boolean): any {
            return fetch(path, { method: 'DELETE' })
                .then(response => {
                    // Check if the Server responded with content
                    if (response.headers.get('Content-Type')?.split(';')[0] === 'application/json' && response.headers.get('Content-Length') !== '0') {
                        return response.json(); // Return the response as JSON
                    } else {
                        return; // Return without content
                    }
                })
                .then(data => {
                    // Check if the Server responded with an error
                    if (data && data.hasOwnProperty('success') && data.success === false) { // If the Server responded with an error
                        if (!disableMessage) this.errorHandler(data, context); // Call the error handler
                        return { success: false };
                    } else { // If the Server responded with a success
                        return { success: true };
                    }
                })
                .catch(error => { // Catch any errors
                    console.error('Error: ', error); // Log the error
                    if (!disableMessage) this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
                    return { success: false };
                })
        },

        // Function to handle errors
        errorHandler(errorData: any, context: string) {
            // console.log('Error: ', errorData, 'Context: ', context)
            let initialErrorMessage = 'Error ' + context + '!';
            let errorMessage = '';
            if(errorData.messages && errorData.messages.length > 0) {
                errorData.messages.forEach((message: any) => {
                    errorMessage += '\n' + message.messageType + (message.code ? '[Code ' + message.code + ']: ' : ': ') + message.text;
                })
            }
            this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', baseError: initialErrorMessage, extendedError: errorMessage });
        },
    },
})
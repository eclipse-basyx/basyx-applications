import { get } from 'lodash';
import { defineStore } from 'pinia';

export const useAuthStore = defineStore({
    id: 'authStore',
    state: () => ({
        token: '' as string | undefined,
        refreshToken: '' as string | undefined,
    }),
    getters: {
        getToken: (state) => state.token,
        getRefreshToken: (state) => state.refreshToken,
    },
    actions: {
        setToken(token: string | undefined) {
            this.token = token;
        },
        setRefreshToken(refreshToken: string | undefined) {
            this.refreshToken = refreshToken;
        },
    }
});

import { defineStore } from 'pinia';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    tokenExpiration: null,
    user: null,
    roles: []
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    getTokenExpiration: (state) => state.tokenExpiration,
    isAdmin: (state) => state.roles.includes('ADMIN'),
  },


  actions: {
    async login(username, password) {
      try {
        const response = await api.post('/api/auth/login', {
          username,
          password
        });

        if (response.data) {
          this.setToken(response.data);
          Notify.create({
            type: 'positive',
            message: 'Successfully logged in'
          });
          return true;
        }
      } catch (error) {
        Notify.create({
          type: 'negative',
          message: error.response?.data?.message || 'Login failed'
        });
        return false;
      }
    },

    async verifyToken() {
      try {
        const response = await api.get('/hello')

        if (response.data) {
          console.log(response.data)
          Notify.create({
            type: 'positive',
            message: 'Successful verify'
          });
          return true;
        }
      } catch (error) {
        Notify.create({
          type: 'negative',
          message: error.response?.data?.message || 'Verify failed'
        });
        return false;
      }
    },

    setToken(token) {
      this.token = token;
      const decoded = jwtDecode(token); // Geändert zu jwtDecode
      this.tokenExpiration = decoded.exp * 1000;
      this.user = decoded.sub;
      this.roles = decoded.roles || [];
      console.log(this.roles)
      console.log(decoded.roles)
      localStorage.setItem('token', token);
    },

    logout() {
      this.token = null;
      this.tokenExpiration = null;
      this.user = null;
      this.roles = [];
      localStorage.removeItem('token');
    },

    hasPermission(requiredRole) {
      return this.roles.includes(requiredRole);
    },

    initializeFromStorage() {
      const token = localStorage.getItem('token');
      if (!token) {
        return;
      }
      this.setToken(token);

      if (!this.tokenExpiration || this.tokenExpiration - Date.now() <= 0) {
          this.logout();
      }

    },
  }
});

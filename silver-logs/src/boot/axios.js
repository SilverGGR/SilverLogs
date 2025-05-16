import { boot } from 'quasar/wrappers';
import axios from 'axios';
import { useAuthStore } from 'stores/auth';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

export default boot(() => {
  api.interceptors.request.use(config => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  });

  api.interceptors.response.use(
    response => response,
    error => {
      if (error.response?.status === 401) {
        const authStore = useAuthStore();
        authStore.logout();
      }
      return Promise.reject(error);
    }
  );
});

export { api };

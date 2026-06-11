import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './styles.css'
import axios from 'axios'

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response ? error.response.status : null;
    const url = error.config ? error.config.url : '';
    if ((status === 401 || status === 403 || error.code === 'ERR_NETWORK' || error.message === 'Network Error') && !url.includes('/login') && !url.includes('/verify-password')) {
      if (window.location.pathname !== '/login') {
        sessionStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

createApp(App).use(router).mount('#app')

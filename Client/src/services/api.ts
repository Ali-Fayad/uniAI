import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_URL } from '../constants';
import { Storage } from '../utils/Storage';

/**
 * Create axios instance with base configuration
 */
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor to add JWT token to headers
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = Storage.getToken();
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor for error handling
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    // Handle 401 Unauthorized - clear storage and redirect to auth
    if (error.response?.status === 401) {
      Storage.clearAll();
      
      // Only redirect if we're not already on the auth pages
      const currentPath = window.location.pathname;
      if (!currentPath.startsWith('/auth') && !currentPath.startsWith('/signin') && !currentPath.startsWith('/signup')) {
        window.location.href = '/auth';
      }
    }
    
    // Handle other error status codes
    if (error.response?.status === 403) {
      console.error('Access forbidden:', error.response.data);
    }
    
    if (error.response?.status === 404) {
      console.error('Resource not found:', error.response.data);
    }
    
    if (error.response?.status >= 500) {
      console.error('Server error:', error.response.data);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;

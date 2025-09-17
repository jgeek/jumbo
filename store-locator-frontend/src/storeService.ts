import axios from 'axios';
import { Store, NearByRequest, ApiErrorResponse, DetailedError } from './types';

// Helper function to get API base URL from environment
const getApiBaseUrl = (): string => {
  // Check if running in browser and env config is available
  if (typeof window !== 'undefined' && (window as any)._env_) {
    return (window as any)._env_.REACT_APP_API_BASE_URL;
  }
  // Fallback to environment variable or default
  return process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';
};

const API_BASE_URL = getApiBaseUrl();

export const storeService = {
  async getClosestStores(params: {
    latitude: number;
    longitude: number;
    maxRadius?: number;
    limit?: number;
    onlyOpen?: boolean;
  }): Promise<Store[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/stores/nearby`, {
        params: {
          latitude: params.latitude,
          longitude: params.longitude,
          maxRadius: params.maxRadius || 5.0,
          limit: params.limit || 5,
          onlyOpen: params.onlyOpen || false
        }
      });
      return response.data;
    } catch (error: any) {
      console.error('Error fetching nearby stores:', error);

      // Parse API error response if available
      if (error.response?.data) {
        const apiError: ApiErrorResponse = error.response.data;
        const detailedError: DetailedError = {
          message: apiError.message || apiError.error || 'An error occurred',
          validationErrors: apiError.validationErrors
        };
        throw detailedError;
      }

      // Fallback for network or other errors
      throw {
        message: 'Failed to fetch nearby stores. Please check your connection and try again.',
        validationErrors: undefined
      } as DetailedError;
    }
  }
};

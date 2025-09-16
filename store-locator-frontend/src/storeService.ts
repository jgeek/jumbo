import axios from 'axios';
import { Store, NearByRequest, ApiErrorResponse, DetailedError } from './types';

const API_BASE_URL = 'http://localhost:8080/api/v1';

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

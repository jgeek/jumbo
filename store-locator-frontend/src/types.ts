export interface Store {
  city: string;
  postalCode: string;
  street: string;
  street2?: string;
  street3?: string;
  addressName?: string;
  uuid: string;
  longitude: number;
  latitude: number;
  complexNumber?: string;
  showWarningMessage: boolean;
  todayOpen?: string;
  todayClose?: string;
  locationType?: string;
  collectionPoint: boolean;
  sapStoreID?: string;
  distance: number;
}

export interface NearByRequest {
  latitude: number;
  longitude: number;
  maxRadius: number;
  limit: number;
  onlyOpen: boolean;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

export interface DetailedError {
  message: string;
  validationErrors?: Record<string, string>;
}

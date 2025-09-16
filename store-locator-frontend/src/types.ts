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

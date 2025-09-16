import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Tooltip } from 'react-leaflet';
import { Store } from './types';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default markers in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface StoreMapProps {
  stores: Store[];
  center: [number, number];
  userLocation?: [number, number] | null;
}

const StoreMap: React.FC<StoreMapProps> = ({ stores, center, userLocation }) => {
  // Custom icon for user location
  const userIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  // Custom icon for stores
  const storeIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  // Function to create a custom div icon with store name
  const createStoreIcon = (storeName: string) => {
    return L.divIcon({
      html: `
        <div style="
          display: flex;
          flex-direction: column;
          align-items: center;
          font-family: Arial, sans-serif;
        ">
          <div style="
            background-color: #FF6900;
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
            white-space: nowrap;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            margin-bottom: 2px;
            max-width: 120px;
            overflow: hidden;
            text-overflow: ellipsis;
          ">
            ${storeName}
          </div>
          <div style="
            width: 0;
            height: 0;
            border-left: 6px solid transparent;
            border-right: 6px solid transparent;
            border-top: 8px solid #FF6900;
            margin-top: -2px;
          "></div>
          <div style="
            width: 16px;
            height: 16px;
            background-color: #FF6900;
            border: 3px solid white;
            border-radius: 50%;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            margin-top: -4px;
          "></div>
        </div>
      `,
      className: 'custom-store-icon',
      iconSize: [140, 60],
      iconAnchor: [70, 50],
      popupAnchor: [0, -50]
    });
  };

  return (
    <MapContainer
      center={center}
      zoom={13}
      style={{ height: '500px', width: '100%' }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* User location marker */}
      {userLocation && (
        <Marker position={userLocation} icon={userIcon}>
          <Popup>
            <div>
              <strong>Your Location</strong>
            </div>
          </Popup>
        </Marker>
      )}

      {/* Store markers with names */}
      {stores.map((store) => {
        const storeName = store.addressName || store.city || 'Jumbo Store';
        const shortName = storeName.length > 15 ? storeName.substring(0, 15) + '...' : storeName;

        return (
          <Marker
            key={store.uuid}
            position={[store.latitude, store.longitude]}
            icon={createStoreIcon(shortName)}
          >
            <Popup>
              <div>
                <h3>Jumbo {store.addressName || store.city}</h3>
                <p><strong>Address:</strong> {store.street}</p>
                <p><strong>City:</strong> {store.city}</p>
                <p><strong>Postal Code:</strong> {store.postalCode}</p>
                {store.todayOpen && store.todayClose && (
                  <p><strong>Hours:</strong> {store.todayOpen} - {store.todayClose}</p>
                )}
                <p><strong>Distance:</strong> {store.distance.toFixed(2)} km</p>
                {store.collectionPoint && (
                  <p><em>Collection Point Available</em></p>
                )}
              </div>
            </Popup>
            {/* Permanent tooltip showing store name on hover */}
            <Tooltip direction="top" offset={[0, -10]} opacity={0.9}>
              <div style={{ textAlign: 'center', fontWeight: 'bold' }}>
                {store.addressName || store.city}
                <br />
                <small>{store.distance.toFixed(2)} km away</small>
              </div>
            </Tooltip>
          </Marker>
        );
      })}
    </MapContainer>
  );
};

export default StoreMap;

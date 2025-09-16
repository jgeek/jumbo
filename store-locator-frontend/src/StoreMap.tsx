import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Tooltip, useMap, useMapEvents } from 'react-leaflet';
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
  onMapDrag?: (lat: number, lng: number, radius?: number, limitChange?: number) => void;
}

// Component to handle map center updates
const MapCenterUpdater: React.FC<{ center: [number, number] }> = ({ center }) => {
  const map = useMap();

  useEffect(() => {
    map.setView(center, map.getZoom());
  }, [center, map]);

  return null;
};

// Component to handle map drag and zoom events
const MapDragHandler: React.FC<{ onMapDrag?: (lat: number, lng: number, radius?: number, limitChange?: number) => void }> = ({ onMapDrag }) => {
  const map = useMap();

  // Track previous zoom level to detect zoom direction
  const [prevZoom, setPrevZoom] = React.useState<number>(map.getZoom());

  // Function to calculate radius based on zoom level
  const calculateRadiusFromZoom = (zoom: number): number => {
    // Zoom level to radius mapping (approximate km coverage)
    // Higher zoom = smaller area = smaller radius
    // Lower zoom = larger area = larger radius
    const zoomToRadius: { [key: number]: number } = {
      18: 0.5,   // Very close zoom
      17: 1,     // Street level
      16: 2,     // Neighborhood
      15: 3,     // Local area
      14: 5,     // District
      13: 8,     // City area (default)
      12: 12,    // Large city area
      11: 18,    // Metropolitan area
      10: 25,    // Regional view
      9: 35,     // Large region
      8: 50,     // Province/state level
      7: 75,     // Multi-province
      6: 100     // Country level (max)
    };

    // Find the closest zoom level or interpolate
    if (zoomToRadius[zoom]) {
      return zoomToRadius[zoom];
    }

    // If exact zoom not found, interpolate between closest values
    const zoomLevels = Object.keys(zoomToRadius).map(Number).sort((a, b) => b - a);

    for (let i = 0; i < zoomLevels.length - 1; i++) {
      const upperZoom = zoomLevels[i];
      const lowerZoom = zoomLevels[i + 1];

      if (zoom <= upperZoom && zoom >= lowerZoom) {
        const upperRadius = zoomToRadius[upperZoom];
        const lowerRadius = zoomToRadius[lowerZoom];
        const ratio = (zoom - lowerZoom) / (upperZoom - lowerZoom);
        return Math.round(lowerRadius + (upperRadius - lowerRadius) * ratio);
      }
    }

    // Fallback: if zoom is outside our range
    if (zoom > 18) return 0.5;  // Very close zoom
    if (zoom < 6) return 100;   // Very far zoom
    return 8; // Default fallback
  };

  useMapEvents({
    dragend: (e) => {
      if (onMapDrag) {
        const center = e.target.getCenter();
        const zoom = e.target.getZoom();
        const radius = calculateRadiusFromZoom(zoom);
        onMapDrag(center.lat, center.lng, radius);
      }
    },
    zoomend: (e) => {
      if (onMapDrag) {
        const center = e.target.getCenter();
        const currentZoom = e.target.getZoom();
        const radius = calculateRadiusFromZoom(currentZoom);

        // Calculate limit change based on zoom direction
        let limitChange = 0;
        if (currentZoom < prevZoom) {
          // Zooming out - increase limit by 10
          limitChange = 10;
        } else if (currentZoom > prevZoom) {
          // Zooming in - decrease limit by 10
          limitChange = -10;
        }

        // Update previous zoom level
        setPrevZoom(currentZoom);

        onMapDrag(center.lat, center.lng, radius, limitChange);
      }
    },
  });

  return null;
};

const StoreMap: React.FC<StoreMapProps> = ({ stores, center, userLocation, onMapDrag }) => {
  // Custom icon for user location - more distinctive
  const userIcon = L.divIcon({
    html: `
      <div style="
        display: flex;
        flex-direction: column;
        align-items: center;
        font-family: Arial, sans-serif;
      ">
        <div style="
          background-color: #2196F3;
          color: white;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 11px;
          font-weight: bold;
          white-space: nowrap;
          box-shadow: 0 2px 4px rgba(0,0,0,0.3);
          margin-bottom: 2px;
        ">
          📍 Your Location
        </div>
        <div style="
          width: 0;
          height: 0;
          border-left: 6px solid transparent;
          border-right: 6px solid transparent;
          border-top: 8px solid #2196F3;
          margin-top: -2px;
        "></div>
        <div style="
          width: 20px;
          height: 20px;
          background: radial-gradient(circle, #2196F3 0%, #1976D2 100%);
          border: 4px solid white;
          border-radius: 50%;
          box-shadow: 0 3px 6px rgba(0,0,0,0.4);
          margin-top: -4px;
          position: relative;
        ">
          <div style="
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 8px;
            height: 8px;
            background-color: white;
            border-radius: 50%;
            animation: pulse 2s infinite;
          "></div>
        </div>
      </div>
      <style>
        @keyframes pulse {
          0% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
          50% { transform: translate(-50%, -50%) scale(1.5); opacity: 0.7; }
          100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
        }
      </style>
    `,
    className: 'custom-user-location-icon',
    iconSize: [120, 60],
    iconAnchor: [60, 50],
    popupAnchor: [0, -50]
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

      {/* Component to update map center when coordinates change */}
      <MapCenterUpdater center={center} />
      <MapDragHandler onMapDrag={onMapDrag} />

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

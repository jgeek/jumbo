import React, { useState, useEffect } from 'react';
import StoreMap from './StoreMap';
import { Store } from './types';
import { storeService } from './storeService';
import './App.css';

const App: React.FC = () => {
  const [stores, setStores] = useState<Store[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [searchParams, setSearchParams] = useState({
    latitude: 52.3702, // Default to Amsterdam
    longitude: 4.8952,
    maxRadius: 5.0,
    limit: 10,
    onlyOpen: false
  });

  // Get user's current location
  const getUserLocation = () => {
    setLoading(true);
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          setUserLocation([lat, lng]);
          setSearchParams(prev => ({
            ...prev,
            latitude: lat,
            longitude: lng
          }));
          setLoading(false);
        },
        (error) => {
          console.error('Error getting location:', error);
          setError('Unable to get your location. Using default location (Amsterdam).');
          setLoading(false);
        }
      );
    } else {
      setError('Geolocation is not supported by this browser.');
      setLoading(false);
    }
  };

  // Fetch nearby stores
  const fetchStores = async () => {
    setLoading(true);
    setError(null);
    try {
      const fetchedStores = await storeService.getClosestStores(searchParams);
      setStores(fetchedStores);
    } catch (err) {
      setError('Failed to fetch nearby stores. Please try again.');
      console.error('Error fetching stores:', err);
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchStores();
  }, []);

  // Update search parameters
  const handleSearchParamChange = (key: string, value: number | boolean) => {
    setSearchParams(prev => ({
      ...prev,
      [key]: value
    }));
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>🛒 Jumbo Store Locator</h1>
        <p>Find your nearest Jumbo stores</p>
      </header>

      <main className="App-main">
        <div className="search-controls">
          <h2>Search Parameters</h2>

          <div className="control-group">
            <label>
              Latitude:
              <input
                type="number"
                step="0.0001"
                value={searchParams.latitude}
                onChange={(e) => handleSearchParamChange('latitude', parseFloat(e.target.value))}
              />
            </label>

            <label>
              Longitude:
              <input
                type="number"
                step="0.0001"
                value={searchParams.longitude}
                onChange={(e) => handleSearchParamChange('longitude', parseFloat(e.target.value))}
              />
            </label>
          </div>

          <div className="control-group">
            <label>
              Max Radius (km):
              <input
                type="number"
                min="1"
                max="100"
                value={searchParams.maxRadius}
                onChange={(e) => handleSearchParamChange('maxRadius', parseFloat(e.target.value))}
              />
            </label>

            <label>
              Max Results:
              <input
                type="number"
                min="1"
                max="50"
                value={searchParams.limit}
                onChange={(e) => handleSearchParamChange('limit', parseInt(e.target.value))}
              />
            </label>
          </div>

          <div className="control-group">
            <label>
              <input
                type="checkbox"
                checked={searchParams.onlyOpen}
                onChange={(e) => handleSearchParamChange('onlyOpen', e.target.checked)}
              />
              Only show open stores
            </label>
          </div>

          <div className="button-group">
            <button onClick={getUserLocation} disabled={loading}>
              📍 Use My Location
            </button>
            <button onClick={fetchStores} disabled={loading}>
              🔍 Search Stores
            </button>
          </div>
        </div>

        {error && (
          <div className="error-message">
            ⚠️ {error}
          </div>
        )}

        {loading && (
          <div className="loading-message">
            🔄 Loading...
          </div>
        )}

        <div className="results-section">
          <h2>Found {stores.length} store(s)</h2>

          <div className="map-container">
            <StoreMap
              stores={stores}
              center={[searchParams.latitude, searchParams.longitude]}
              userLocation={userLocation}
            />
          </div>

          <div className="store-list">
            <h3>Store Details</h3>
            {stores.length === 0 && !loading && (
              <p>No stores found. Try adjusting your search parameters.</p>
            )}
            {stores.map((store) => (
              <div key={store.uuid} className="store-card">
                <h4>Jumbo {store.addressName || store.city}</h4>
                <p><strong>📍 Address:</strong> {store.street}, {store.city} {store.postalCode}</p>
                {store.todayOpen && store.todayClose && (
                  <p><strong>🕒 Hours:</strong> {store.todayOpen} - {store.todayClose}</p>
                )}
                <p><strong>📏 Distance:</strong> {store.distance.toFixed(2)} km</p>
                {store.collectionPoint && (
                  <p><strong>📦 Collection Point:</strong> Available</p>
                )}
                {store.showWarningMessage && (
                  <p className="warning">⚠️ Special notice for this store</p>
                )}
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
};

export default App;

import React, { useState, useEffect } from 'react';
import StoreMap from './StoreMap';
import { Store, DetailedError } from './types';
import { storeService } from './storeService';
import './App.css';

const App: React.FC = () => {
  const [stores, setStores] = useState<Store[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<DetailedError | null>(null);
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);

  // Separate input coordinates from map center coordinates
  const [inputCoords, setInputCoords] = useState({
    latitude: 52.3702, // Default to Amsterdam
    longitude: 4.8952,
  });

  const [mapCenter, setMapCenter] = useState<[number, number]>([52.3702, 4.8952]);

  const [searchParams, setSearchParams] = useState({
    latitude: 52.3702,
    longitude: 4.8952,
    maxRadius: 5.0,
    limit: 10,
    onlyOpen: false
  });

  // Famous cities in Netherlands
  const famousCities = [
    // Netherlands
    { name: 'Amsterdam', country: 'Netherlands', latitude: 52.3676, longitude: 4.9041, flag: '🇳🇱' },
    { name: 'Rotterdam', country: 'Netherlands', latitude: 51.9244, longitude: 4.4777, flag: '🇳🇱' },
    { name: 'The Hague', country: 'Netherlands', latitude: 52.0705, longitude: 4.3007, flag: '🇳🇱' },
    { name: 'Utrecht', country: 'Netherlands', latitude: 52.0907, longitude: 5.1214, flag: '🇳🇱' },
    { name: 'Eindhoven', country: 'Netherlands', latitude: 51.4416, longitude: 5.4697, flag: '🇳🇱' },
    { name: 'Tilburg', country: 'Netherlands', latitude: 51.5555, longitude: 5.0913, flag: '🇳🇱' },
    { name: 'Groningen', country: 'Netherlands', latitude: 53.2194, longitude: 6.5665, flag: '🇳🇱' },
    { name: 'Almere', country: 'Netherlands', latitude: 52.3508, longitude: 5.2647, flag: '🇳🇱' },
    { name: 'Breda', country: 'Netherlands', latitude: 51.5719, longitude: 4.7683, flag: '🇳🇱' },
    { name: 'Nijmegen', country: 'Netherlands', latitude: 51.8426, longitude: 5.8517, flag: '🇳🇱' },
    { name: 'Veghel', country: 'Netherlands', latitude: 51.6161, longitude: 5.5522, flag: '🇳🇱' }
  ];

  // Get user's current location
  const getUserLocation = () => {
    setLoading(true);
    setError(null);

    if (!navigator.geolocation) {
      setError({ message: 'Geolocation is not supported by this browser.' });
      setLoading(false);
      return;
    }

    // Enhanced geolocation options
    const options = {
      enableHighAccuracy: true,
      timeout: 10000, // 10 seconds timeout
      maximumAge: 300000 // Accept cached position up to 5 minutes old
    };

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        setUserLocation([lat, lng]);
        setInputCoords({
          latitude: lat,
          longitude: lng
        });
        setMapCenter([lat, lng]);
        setSearchParams(prev => ({
          ...prev,
          latitude: lat,
          longitude: lng
        }));
        setLoading(false);
        setError(null);
        console.log('Location found:', { lat, lng, accuracy: position.coords.accuracy });
      },
      (error) => {
        console.error('Geolocation error:', error);
        setLoading(false);

        let errorMessage = '';
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Location access denied. Please allow location access in your browser settings and try again.';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Location information is unavailable. This might be due to poor GPS signal or network issues. Using default location (Amsterdam).';
            break;
          case error.TIMEOUT:
            errorMessage = 'Location request timed out. Please try again or check your GPS/network connection.';
            break;
          default:
            errorMessage = 'An unknown error occurred while retrieving location. Using default location (Amsterdam).';
            break;
        }
        setError({ message: errorMessage });
      },
      options
    );
  };

  // Fetch nearby stores and update map center
  const fetchStores = async () => {
    setLoading(true);
    setError(null);

    // Update search params with current input coordinates
    const currentSearchParams = {
      ...searchParams,
      latitude: inputCoords.latitude,
      longitude: inputCoords.longitude
    };

    // Update map center to the search location
    setMapCenter([inputCoords.latitude, inputCoords.longitude]);

    try {
      const fetchedStores = await storeService.getClosestStores(currentSearchParams);
      setStores(fetchedStores);
      setSearchParams(currentSearchParams);
    } catch (err: any) {
      // Handle DetailedError from storeService or fallback to simple message
      if (err && typeof err === 'object' && 'message' in err) {
        setError(err as DetailedError);
      } else {
        setError({ message: 'Failed to fetch nearby stores. Please try again.' });
      }
      console.error('Error fetching stores:', err);
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchStores();
  }, []);

  // Update input coordinates (doesn't trigger map center change)
  const handleCoordChange = (key: 'latitude' | 'longitude', value: number) => {
    setInputCoords(prev => ({
      ...prev,
      [key]: value
    }));
  };

  // Update other search parameters
  const handleSearchParamChange = (key: string, value: number | boolean) => {
    setSearchParams(prev => ({
      ...prev,
      [key]: value
    }));
  };

  // Set location to a famous city
  const setLocationToCity = (city: typeof famousCities[0]) => {
    setInputCoords({
      latitude: city.latitude,
      longitude: city.longitude
    });
    setMapCenter([city.latitude, city.longitude]);
    setUserLocation(null); // Clear user's GPS location when selecting a city

    // Automatically search for stores in the selected city
    const citySearchParams = {
      ...searchParams,
      latitude: city.latitude,
      longitude: city.longitude
    };

    setSearchParams(citySearchParams);
    searchStoresInCity(citySearchParams);
  };

  // Search for stores in selected city
  const searchStoresInCity = async (cityParams: typeof searchParams) => {
    setLoading(true);
    setError(null);

    try {
      const fetchedStores = await storeService.getClosestStores(cityParams);
      setStores(fetchedStores);
    } catch (err: any) {
      // Handle DetailedError from storeService or fallback to simple message
      if (err && typeof err === 'object' && 'message' in err) {
        setError(err as DetailedError);
      } else {
        setError({ message: 'Failed to fetch stores in the selected city. Please try again.' });
      }
      console.error('Error fetching stores:', err);
    } finally {
      setLoading(false);
    }
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

          {/* Famous Cities Section */}
          <div className="cities-section">
            <h3>🏙️ Quick Select - Famous Cities</h3>
            <div className="cities-grid">
              {famousCities.map((city) => (
                <button
                  key={`${city.name}-${city.country}`}
                  className="city-button"
                  onClick={() => setLocationToCity(city)}
                  disabled={loading}
                >
                  {city.flag} {city.name}
                  <small>{city.country}</small>
                </button>
              ))}
            </div>
          </div>

          <div className="control-group">
            <label>
              Latitude:
              <input
                type="number"
                step="0.0001"
                value={inputCoords.latitude}
                onChange={(e) => handleCoordChange('latitude', parseFloat(e.target.value))}
              />
            </label>

            <label>
              Longitude:
              <input
                type="number"
                step="0.0001"
                value={inputCoords.longitude}
                onChange={(e) => handleCoordChange('longitude', parseFloat(e.target.value))}
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
            <div className="error-main">
              ⚠️ {error.message}
            </div>
            {error.validationErrors && Object.keys(error.validationErrors).length > 0 && (
              <div className="validation-errors">
                <h4>Validation Errors:</h4>
                <ul>
                  {Object.entries(error.validationErrors).map(([field, message]) => (
                    <li key={field}>
                      <strong>{field}:</strong> {message}
                    </li>
                  ))}
                </ul>
              </div>
            )}
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
              center={mapCenter}
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

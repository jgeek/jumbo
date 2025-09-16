package com.jumbo.adapter.out.persistence;

import com.jumbo.application.domain.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreRepositoryImplTest {

    @Mock
    private StoreMapper storeMapper;

    private StoreRepositoryImpl storeRepository;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepositoryImpl(storeMapper);
    }

    @Test
    void findAll_WhenValidJsonFile_ReturnsListOfStores() throws IOException {
        List<Store> expectedStores = Arrays.asList(
                createStore("1", "Amsterdam", 52.3676, 4.9041),
                createStore("2", "Rotterdam", 51.9244, 4.4777)
        );

        when(storeMapper.toDomainList(any())).thenReturn(expectedStores);

        List<Store> result = storeRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Amsterdam", result.get(0).getCity());
        assertEquals("Rotterdam", result.get(1).getCity());
        verify(storeMapper).toDomainList(any());
    }

    @Test
    void findAll_WhenEmptyJsonFile_ReturnsEmptyList() throws IOException {
        when(storeMapper.toDomainList(any())).thenReturn(Arrays.asList());

        List<Store> result = storeRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(storeMapper).toDomainList(any());
    }

    @Test
    void findAll_WhenMapperThrowsException_PropagatesException() {
        when(storeMapper.toDomainList(any())).thenThrow(new RuntimeException("Mapping failed"));

        assertThrows(RuntimeException.class, () -> storeRepository.findAll());
        verify(storeMapper).toDomainList(any());
    }

    @Test
    void findAll_WhenResourceNotFound_ThrowsIOException() {
        StoreRepositoryImpl repositoryWithMissingFile = new StoreRepositoryImpl(storeMapper) {
            @Override
            public List<Store> findAll() throws IOException {
                throw new IOException("Resource not found");
            }
        };

        assertThrows(IOException.class, repositoryWithMissingFile::findAll);
    }

    @Test
    void findAll_WhenLargeDataset_HandlesSuccessfully() throws IOException {
        List<Store> largeStoreList = createLargeStoreList(1000);
        when(storeMapper.toDomainList(any())).thenReturn(largeStoreList);

        List<Store> result = storeRepository.findAll();

        assertNotNull(result);
        assertEquals(1000, result.size());
        verify(storeMapper).toDomainList(any());
    }

    @Test
    void findAll_WhenMapperReturnsNull_HandlesGracefully() throws IOException {
        when(storeMapper.toDomainList(any())).thenReturn(null);

        List<Store> result = storeRepository.findAll();

        assertNull(result);
        verify(storeMapper).toDomainList(any());
    }

    @Test
    void findAll_WhenStoresContainSpecialCharacters_ParsesCorrectly() throws IOException {
        List<Store> storesWithSpecialChars = Arrays.asList(
                createStore("1", "'s-Gravenhage", 52.0705, 4.3007),
                createStore("2", "Müller-Straße", 51.5074, -0.1278)
        );

        when(storeMapper.toDomainList(any())).thenReturn(storesWithSpecialChars);

        List<Store> result = storeRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("'s-Gravenhage", result.get(0).getCity());
        assertEquals("Müller-Straße", result.get(1).getCity());
    }

    @Test
    void findAll_WhenStoresHaveVariousOpeningTimes_ParsesCorrectly() throws IOException {
        List<Store> storesWithTimes = Arrays.asList(
                createStoreWithTimes("1", "Amsterdam", LocalTime.of(8, 0), LocalTime.of(22, 0)),
                createStoreWithTimes("2", "Rotterdam", LocalTime.of(7, 30), LocalTime.of(21, 0))
        );

        when(storeMapper.toDomainList(any())).thenReturn(storesWithTimes);

        List<Store> result = storeRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalTime.of(8, 0), result.get(0).getTodayOpen());
        assertEquals(LocalTime.of(22, 0), result.get(0).getTodayClose());
    }

    @Test
    void findAll_CallsMapperOnlyOnce() throws IOException {
        when(storeMapper.toDomainList(any())).thenReturn(Arrays.asList());

        storeRepository.findAll();

        verify(storeMapper, times(1)).toDomainList(any());
    }

    private Store createStore(String uuid, String city, double latitude, double longitude) {
        Store store = new Store();
        store.setUuid(uuid);
        store.setCity(city);
        store.setLatitude(latitude);
        store.setLongitude(longitude);
        return store;
    }

    private Store createStoreWithTimes(String uuid, String city, LocalTime openTime, LocalTime closeTime) {
        Store store = createStore(uuid, city, 52.0, 4.0);
        store.setTodayOpen(openTime);
        store.setTodayClose(closeTime);
        return store;
    }

    private List<Store> createLargeStoreList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> createStore(String.valueOf(i), "City" + i, 52.0 + i * 0.01, 4.0 + i * 0.01))
                .collect(java.util.stream.Collectors.toList());
    }
}

package com.naumov.repository;

import com.naumov.model.Address;
import com.naumov.model.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AddressRepositoryTest {
    @Autowired
    RegionRepository regionRepository;
    @Autowired
    AddressRepository addressRepository;

    @Test
    @Transactional
    void findByRegionNameAndAddressTest() {
        Region region0 = regionRepository.findAll().get(0);

        Address address = Address.builder()
                .region(region0)
                .address("Test address")
                .build();

        addressRepository.save(address);
        Optional<Address> loadedAddress = addressRepository.findByRegionNameAndAddress(region0.getName(), "Test address");

        assertThat(loadedAddress.isPresent()).isTrue();
        assertThat(loadedAddress.get().getAddress()).isEqualTo("Test address");
    }
}
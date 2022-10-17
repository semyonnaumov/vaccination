package com.naumov.repository;

import com.naumov.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByName(String name);
}

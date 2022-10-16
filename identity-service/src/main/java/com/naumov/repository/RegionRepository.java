package com.naumov.repository;

import com.naumov.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RegionRepository extends JpaRepository<Region, Long> {
    Region findByName(String name);
}

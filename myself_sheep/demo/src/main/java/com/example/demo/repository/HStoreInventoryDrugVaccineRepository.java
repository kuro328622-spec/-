package com.example.demo.repository;

import com.example.demo.entity.HStoreInventoryDrugVaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 总库存 Repository
 */
@Repository
public interface HStoreInventoryDrugVaccineRepository extends JpaRepository<HStoreInventoryDrugVaccine, Long> {

    /**
     * 核心精确查询方法：厂家 + 药品名称 + 类别 解决同名同厂家但不同类别导致的 NonUniqueResultException
     */
    Optional<HStoreInventoryDrugVaccine> findByManufacturerAndNameAndCategory(
            String manufacturer,
            String name,
            String category
    );

    /**
     * 备用查询方法：名称 + 厂家 + 类别
     */
    Optional<HStoreInventoryDrugVaccine> findByNameAndManufacturerAndCategory(
            String name,
            String manufacturer,
            String category
    );
}

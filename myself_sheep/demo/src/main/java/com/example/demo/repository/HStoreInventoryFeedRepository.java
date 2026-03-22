package com.example.demo.repository;

import com.example.demo.entity.HStoreInventoryFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HStoreInventoryFeedRepository extends JpaRepository<HStoreInventoryFeed, Long> {

    /**
     * 核心精确查询方法：厂家 + 饲料名称 (f_name) + 类别 (type) + 成分类型
     * 用于在出库、修改或删除时，精准定位到唯一的库存记录进行数量对冲
     */
    Optional<HStoreInventoryFeed> findByManufacturerAndNameAndCategoryAndComponentType(
            String manufacturer,
            String name,
            String category,
            String componentType
    );

    /**
     * 备用查询方法
     */
    Optional<HStoreInventoryFeed> findByNameAndManufacturerAndCategoryAndComponentType(
            String name,
            String manufacturer,
            String category,
            String componentType
    );
}

package com.example.demo.repository;

import com.example.demo.entity.DrugVaccineStockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DrugVaccineStockOutRepository extends JpaRepository<DrugVaccineStockOut, Long> {

    /**
     * 查询当天出库记录，用于生成出库单号
     *
     * @param start 当天起始时间
     * @param end 当天结束时间
     * @return 当天所有出库记录列表
     */
    List<DrugVaccineStockOut> findByDeliveryTimeBetween(Date start, Date end);

    // 注意：JpaRepository 已经提供 findAll(), findById(), save(), delete() 等方法
    // 直接在 Service 中使用即可，无需额外声明
}

package com.example.demo.repository;

import com.example.demo.entity.FeedStockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface FeedStockOutRepository extends JpaRepository<FeedStockOut, Long> {

    /**
     * 查询指定时间范围内的出库记录 核心用途：供 OutboundNoGenerator 调用，根据当天已有的记录数生成自增单号
     */
    List<FeedStockOut> findByDeliveryTimeBetween(Date start, Date end);
}

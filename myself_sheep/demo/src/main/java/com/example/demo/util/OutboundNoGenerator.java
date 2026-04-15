package com.example.demo.util;

import com.example.demo.entity.DrugVaccineStockOut;
import com.example.demo.repository.DrugVaccineStockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class OutboundNoGenerator {

    @Autowired
    private DrugVaccineStockOutRepository stockOutRepository;

    /**
     * 生成出库单号，格式：OUT-YYYYMMDD-XXXX
     */
    public synchronized String generateOutboundNo() {
        // 获取当前日期
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(now);

        // 查询当天所有出库记录
        List<DrugVaccineStockOut> todayOuts = stockOutRepository.findByDeliveryTimeBetween(
                getStartOfDay(now), getEndOfDay(now));

        int seq = todayOuts.size() + 1;

        // 序号格式化为 4 位，不够补 0
        DecimalFormat df = new DecimalFormat("0000");
        String seqStr = df.format(seq);

        return "OUT-" + dateStr + "-" + seqStr;
    }

    // 获取当天开始时间
    private Date getStartOfDay(Date date) {
        return java.sql.Timestamp.valueOf(new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00").format(date));
    }

    // 获取当天结束时间
    private Date getEndOfDay(Date date) {
        return java.sql.Timestamp.valueOf(new java.text.SimpleDateFormat("yyyy-MM-dd 23:59:59").format(date));
    }
}

package com.example.demo.service.impl;

import com.example.demo.entity.ManufacturerInfo;
import com.example.demo.repository.ManufacturerInfoRepository;
import com.example.demo.service.ManufacturerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ManufacturerInfoServiceImpl implements ManufacturerInfoService {

    @Autowired
    private ManufacturerInfoRepository manufacturerRepository;

    @Override
    @Transactional
    public ManufacturerInfo save(ManufacturerInfo manufacturer) {

        // 1. 唯一性验证：防止重复录入厂家
        Optional<ManufacturerInfo> existing = manufacturerRepository.findByManufacturerName(manufacturer.getManufacturerName());
        if (existing.isPresent()) {
            throw new RuntimeException("生产厂家 [" + manufacturer.getManufacturerName() + "] 已存在，请勿重复添加");
        }

        // 2. 设置初始数据
        if (manufacturer.getCreateTime() == null) {
            manufacturer.setCreateTime(new Date());
        }

        return manufacturerRepository.save(manufacturer);
    }

    @Override
    @Transactional
    public ManufacturerInfo update(Long id, ManufacturerInfo newInfo) {

        // 1. 查找旧记录
        ManufacturerInfo oldInfo = manufacturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("厂家记录不存在，ID：" + id));

        // 2. 更新字段
        oldInfo.setManufacturerName(newInfo.getManufacturerName());
        oldInfo.setManCategory(newInfo.getManCategory());
        oldInfo.setManContactPerson(newInfo.getManContactPerson());
        oldInfo.setManContactPhone(newInfo.getManContactPhone());
        oldInfo.setManContactEmail(newInfo.getManContactEmail());
        oldInfo.setManContactAddress(newInfo.getManContactAddress());
        oldInfo.setManRemark(newInfo.getManRemark());

        return manufacturerRepository.save(oldInfo);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        if (!manufacturerRepository.existsById(id)) {
            throw new RuntimeException("厂家记录不存在，ID：" + id);
        }

        manufacturerRepository.deleteById(id);
    }

    @Override
    public List<ManufacturerInfo> findAll() {
        return manufacturerRepository.findAll();
    }

    @Override
    public Optional<ManufacturerInfo> findById(Long id) {
        return manufacturerRepository.findById(id);
    }

    @Override
    public Optional<ManufacturerInfo> findByName(String name) {
        return manufacturerRepository.findByManufacturerName(name);
    }

    /**
     * 私有辅助方法：校验是否为管理员角色
     */
    private void checkAdminRole(String role) {
        if (!"admin".equalsIgnoreCase(role)) {
            throw new RuntimeException("权限不足：只有管理员可以操作厂家名录");
        }
    }
}

package com.vrs.repository;

import com.vrs.entity.SparePartOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartOrderRepository extends JpaRepository<SparePartOrder, Long> {
    List<SparePartOrder> findByVehicleId(Long vehicleId);

    List<SparePartOrder> findAllByOrderByIdDesc();
}

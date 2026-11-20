package com.vrs.service;

import com.vrs.entity.SparePartOrder;
import com.vrs.entity.Vehicle;
import com.vrs.repository.SparePartOrderRepository;
import com.vrs.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SparePartOrderService {

    private final SparePartOrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;

    public SparePartOrderService(SparePartOrderRepository orderRepository, VehicleRepository vehicleRepository) {
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public SparePartOrder placeOrder(Long vehicleId, String partName, Integer quantity, Double unitPrice) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle selection is required.");
        }
        if (partName == null || partName.isBlank()) {
            throw new IllegalArgumentException("Part name is required.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Registered vehicle not found (ID: " + vehicleId + ")."));

        SparePartOrder order = new SparePartOrder();
        order.setVehicle(vehicle);
        order.setPartName(partName.trim());
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice != null && unitPrice >= 0 ? unitPrice : 0.0);
        order.setStatus("ORDERED");
        order.setOrderDate(LocalDate.now());

        return orderRepository.save(order);
    }

    public List<SparePartOrder> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public List<SparePartOrder> getOrdersByVehicleId(Long vehicleId) {
        return orderRepository.findByVehicleId(vehicleId);
    }

    @Transactional
    public SparePartOrder updateOrderStatus(Long orderId, String status) {
        SparePartOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required.");
        }
        order.setStatus(status.trim().toUpperCase());
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}

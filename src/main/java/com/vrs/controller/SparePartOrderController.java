package com.vrs.controller;

import com.vrs.dto.SparePartOrderRequest;
import com.vrs.entity.SparePartOrder;
import com.vrs.service.SparePartOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class SparePartOrderController {

    private final SparePartOrderService orderService;

    public SparePartOrderController(SparePartOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<SparePartOrder> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<SparePartOrder> getOrdersByVehicle(@PathVariable Long vehicleId) {
        return orderService.getOrdersByVehicleId(vehicleId);
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody SparePartOrderRequest request) {
        try {
            SparePartOrder saved = orderService.placeOrder(
                    request.getVehicleId(),
                    request.getPartName(),
                    request.getQuantity(),
                    request.getUnitPrice()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            SparePartOrder updated = orderService.updateOrderStatus(id, body.get("status"));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(Map.of("message", "Order deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

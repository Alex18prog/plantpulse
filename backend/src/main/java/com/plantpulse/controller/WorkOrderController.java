package com.plantpulse.controller;

import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.WorkOrderStatus;
import com.plantpulse.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public List<WorkOrder> findAll() {
        return workOrderService.findAll();
    }

    @PostMapping
    public WorkOrder create(@Valid @RequestBody WorkOrder workOrder) {
        workOrder.setId(null);
        return workOrderService.create(workOrder);
    }

    @PatchMapping("/{id}/status")
    public WorkOrder updateStatus(@PathVariable Long id, @RequestParam WorkOrderStatus status) {
        return workOrderService.updateStatus(id, status);
    }
}

package com.plantpulse.controller;

import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.WorkOrderStatus;
import com.plantpulse.security.AppUserPrincipal;
import com.plantpulse.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public List<WorkOrder> findAll(@AuthenticationPrincipal AppUserPrincipal principal) {
        return workOrderService.findAllVisibleTo(principal.getUser());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public WorkOrder create(@Valid @RequestBody WorkOrder workOrder) {
        workOrder.setId(null);
        return workOrderService.create(workOrder);
    }

    @PatchMapping("/{id}/status")
    public WorkOrder updateStatus(@PathVariable Long id, @RequestParam WorkOrderStatus status,
                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return workOrderService.updateStatus(id, status, principal.getUser());
    }
}

package com.plantpulse.controller;

import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.WorkOrderStatus;
import com.plantpulse.security.AppUserPrincipal;
import com.plantpulse.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    @Operation(
            summary = "List work orders visible to the caller",
            description = "ADMIN gets every work order. TECHNICIAN gets only the ones assigned to their "
                    + "linked Technician record — this is a server-side filter, not a client concern."
    )
    public List<WorkOrder> findAll(@AuthenticationPrincipal AppUserPrincipal principal) {
        return workOrderService.findAllVisibleTo(principal.getUser());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually create a work order (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public WorkOrder create(@Valid @RequestBody WorkOrder workOrder) {
        workOrder.setId(null);
        return workOrderService.create(workOrder);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update a work order's status",
            description = "ADMIN can update any work order. TECHNICIAN can only update one assigned to "
                    + "their linked Technician record; targeting any other work order is a 403, not a 404 "
                    + "(the order isn't hidden, just not theirs to change)."
    )
    @ApiResponse(responseCode = "403", description = "TECHNICIAN caller is not assigned to this work order")
    public WorkOrder updateStatus(@PathVariable Long id, @RequestParam WorkOrderStatus status,
                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return workOrderService.updateStatus(id, status, principal.getUser());
    }
}

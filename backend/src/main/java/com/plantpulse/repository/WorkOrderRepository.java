package com.plantpulse.repository;

import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByMachineId(Long machineId);
    List<WorkOrder> findByTechnicianId(Long technicianId);
}

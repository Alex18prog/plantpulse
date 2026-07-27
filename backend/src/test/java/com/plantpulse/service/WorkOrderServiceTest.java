package com.plantpulse.service;

import com.plantpulse.domain.Machine;
import com.plantpulse.domain.Technician;
import com.plantpulse.domain.User;
import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.MachineStatus;
import com.plantpulse.domain.enums.Priority;
import com.plantpulse.domain.enums.Role;
import com.plantpulse.domain.enums.TechnicianStatus;
import com.plantpulse.domain.enums.WorkOrderStatus;
import com.plantpulse.domain.enums.WorkOrderType;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

    @Captor
    private ArgumentCaptor<WorkOrder> workOrderCaptor;

    private Machine machine;
    private Technician marta;
    private Technician jon;
    private User adminUser;
    private User martaUser;
    private User jonUser;

    @BeforeEach
    void setUp() {
        machine = Machine.builder()
                .id(1L)
                .name("CNC Lathe #1")
                .type("CNC Lathe")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(58.0)
                .baselineVibration(2.2)
                .build();

        marta = Technician.builder().id(10L).name("Marta Ruiz").status(TechnicianStatus.AVAILABLE).build();
        jon = Technician.builder().id(20L).name("Jon Etxeberria").status(TechnicianStatus.AVAILABLE).build();

        adminUser = User.builder().id(1L).email("admin@plantpulse.dev").role(Role.ADMIN).build();
        martaUser = User.builder().id(2L).email("marta.ruiz@plantpulse.dev").role(Role.TECHNICIAN).technician(marta).build();
        jonUser = User.builder().id(3L).email("jon.etxeberria@plantpulse.dev").role(Role.TECHNICIAN).technician(jon).build();
    }

    @Test
    void findAllVisibleTo_admin_returnsEveryWorkOrder() {
        List<WorkOrder> orders = List.of(WorkOrder.builder().id(1L).build(), WorkOrder.builder().id(2L).build());
        when(workOrderRepository.findAll()).thenReturn(orders);

        List<WorkOrder> result = workOrderService.findAllVisibleTo(adminUser);

        assertThat(result).isEqualTo(orders);
        verify(workOrderRepository, never()).findByTechnicianId(any());
    }

    @Test
    void findAllVisibleTo_technician_returnsOnlyOwnAssignedOrders() {
        List<WorkOrder> martaOrders = List.of(WorkOrder.builder().id(5L).technician(marta).build());
        when(workOrderRepository.findByTechnicianId(10L)).thenReturn(martaOrders);

        List<WorkOrder> result = workOrderService.findAllVisibleTo(martaUser);

        assertThat(result).isEqualTo(martaOrders);
        verify(workOrderRepository, never()).findAll();
    }

    @Test
    void create_savesAndReturnsWorkOrder() {
        WorkOrder toCreate = WorkOrder.builder().machine(machine).type(WorkOrderType.PREVENTIVE).build();
        WorkOrder saved = WorkOrder.builder().id(10L).machine(machine).type(WorkOrderType.PREVENTIVE).build();
        when(workOrderRepository.save(toCreate)).thenReturn(saved);

        WorkOrder result = workOrderService.create(toCreate);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void updateStatus_toDone_setsClosedAt() {
        WorkOrder order = WorkOrder.builder()
                .id(5L)
                .machine(machine)
                .status(WorkOrderStatus.IN_PROGRESS)
                .build();
        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.updateStatus(5L, WorkOrderStatus.DONE, adminUser);

        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.DONE);
        assertThat(result.getClosedAt()).isNotNull();
    }

    @Test
    void updateStatus_toInProgress_doesNotSetClosedAt() {
        WorkOrder order = WorkOrder.builder()
                .id(5L)
                .machine(machine)
                .status(WorkOrderStatus.PENDING)
                .build();
        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.updateStatus(5L, WorkOrderStatus.IN_PROGRESS, adminUser);

        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(result.getClosedAt()).isNull();
    }

    @Test
    void updateStatus_unknownId_throwsResourceNotFoundException() {
        when(workOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.updateStatus(99L, WorkOrderStatus.DONE, adminUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void updateStatus_technicianAssignedToOrder_canUpdate() {
        WorkOrder order = WorkOrder.builder().id(5L).machine(machine).technician(marta).status(WorkOrderStatus.PENDING).build();
        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.updateStatus(5L, WorkOrderStatus.IN_PROGRESS, martaUser);

        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_technicianNotAssignedToOrder_throwsAccessDenied() {
        WorkOrder order = WorkOrder.builder().id(5L).machine(machine).technician(marta).status(WorkOrderStatus.PENDING).build();
        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> workOrderService.updateStatus(5L, WorkOrderStatus.IN_PROGRESS, jonUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void updateStatus_technicianOnUnassignedOrder_throwsAccessDenied() {
        WorkOrder order = WorkOrder.builder().id(5L).machine(machine).technician(null).status(WorkOrderStatus.PENDING).build();
        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> workOrderService.updateStatus(5L, WorkOrderStatus.IN_PROGRESS, martaUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void openAutoCorrectiveOrder_noExistingOrders_createsCriticalCorrectiveOrder() {
        when(workOrderRepository.findByMachineId(1L)).thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.openAutoCorrectiveOrder(machine, "Critical reading on CNC Lathe #1: 95.0C");

        verify(workOrderRepository).save(workOrderCaptor.capture());
        WorkOrder saved = workOrderCaptor.getValue();
        assertThat(saved.getMachine()).isEqualTo(machine);
        assertThat(saved.getType()).isEqualTo(WorkOrderType.CORRECTIVE);
        assertThat(saved.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
        assertThat(saved.getPriority()).isEqualTo(Priority.CRITICAL);
        assertThat(saved.isAutoGenerated()).isTrue();
        assertThat(saved.getDescription()).contains("Critical reading on CNC Lathe #1: 95.0C");
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void openAutoCorrectiveOrder_existingOpenAutoGeneratedOrder_returnsNullAndDoesNotSave() {
        WorkOrder existingOpen = WorkOrder.builder()
                .id(3L)
                .machine(machine)
                .autoGenerated(true)
                .status(WorkOrderStatus.PENDING)
                .build();
        when(workOrderRepository.findByMachineId(1L)).thenReturn(List.of(existingOpen));

        WorkOrder result = workOrderService.openAutoCorrectiveOrder(machine, "Another critical reading");

        assertThat(result).isNull();
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void openAutoCorrectiveOrder_existingOrderAlreadyDone_createsNewOrder() {
        WorkOrder closedOrder = WorkOrder.builder()
                .id(3L)
                .machine(machine)
                .autoGenerated(true)
                .status(WorkOrderStatus.DONE)
                .build();
        when(workOrderRepository.findByMachineId(1L)).thenReturn(List.of(closedOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.openAutoCorrectiveOrder(machine, "New critical reading");

        assertThat(result).isNotNull();
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void openAutoCorrectiveOrder_existingManualOrderStillOpen_createsAutoOrderAnyway() {
        WorkOrder manualOrder = WorkOrder.builder()
                .id(4L)
                .machine(machine)
                .autoGenerated(false)
                .status(WorkOrderStatus.PENDING)
                .build();
        when(workOrderRepository.findByMachineId(1L)).thenReturn(List.of(manualOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = workOrderService.openAutoCorrectiveOrder(machine, "Critical reading");

        assertThat(result).isNotNull();
        verify(workOrderRepository).save(any(WorkOrder.class));
    }
}

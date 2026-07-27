package com.plantpulse.scheduler;

import com.plantpulse.domain.Alert;
import com.plantpulse.domain.Machine;
import com.plantpulse.domain.WorkOrder;
import com.plantpulse.domain.enums.AlertSeverity;
import com.plantpulse.domain.enums.MachineStatus;
import com.plantpulse.dto.AlertMessage;
import com.plantpulse.repository.AlertRepository;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.service.WorkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetrySimulatorServiceTest {

    private static final double TEMP_WARNING = 75.0;
    private static final double TEMP_CRITICAL = 90.0;
    private static final double VIB_WARNING = 6.0;
    private static final double VIB_CRITICAL = 9.0;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private WorkOrderService workOrderService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TelemetrySimulatorService telemetrySimulatorService;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    @Captor
    private ArgumentCaptor<AlertMessage> alertMessageCaptor;

    private Machine machine;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(telemetrySimulatorService, "tempWarning", TEMP_WARNING);
        ReflectionTestUtils.setField(telemetrySimulatorService, "tempCritical", TEMP_CRITICAL);
        ReflectionTestUtils.setField(telemetrySimulatorService, "vibWarning", VIB_WARNING);
        ReflectionTestUtils.setField(telemetrySimulatorService, "vibCritical", VIB_CRITICAL);

        machine = Machine.builder()
                .id(1L)
                .name("CNC Lathe #1")
                .type("CNC Lathe")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(58.0)
                .baselineVibration(2.2)
                .build();
    }

    @Test
    void belowAllThresholds_raisesNoAlertAndOpensNoWorkOrder() {
        telemetrySimulatorService.evaluateThresholds(machine, 60.0, 2.5);

        verifyNoInteractions(alertRepository, workOrderService, messagingTemplate);
    }

    @Test
    void temperatureAtCriticalThreshold_raisesCriticalAlertAndOpensWorkOrder() {
        WorkOrder autoOrder = WorkOrder.builder().id(42L).build();
        when(workOrderService.openAutoCorrectiveOrder(eq(machine), anyString())).thenReturn(autoOrder);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        telemetrySimulatorService.evaluateThresholds(machine, TEMP_CRITICAL, 2.5);

        verify(workOrderService).openAutoCorrectiveOrder(eq(machine), anyString());
        verify(alertRepository).save(alertCaptor.capture());
        Alert saved = alertCaptor.getValue();
        assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(saved.getMachine()).isEqualTo(machine);
        assertThat(saved.getLinkedWorkOrder()).isEqualTo(autoOrder);

        verify(messagingTemplate).convertAndSend(eq("/topic/alerts"), alertMessageCaptor.capture());
        assertThat(alertMessageCaptor.getValue().severity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(alertMessageCaptor.getValue().linkedWorkOrderId()).isEqualTo(42L);
    }

    @Test
    void vibrationAtCriticalThreshold_raisesCriticalAlertAndOpensWorkOrder() {
        when(workOrderService.openAutoCorrectiveOrder(eq(machine), anyString()))
                .thenReturn(WorkOrder.builder().id(7L).build());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        telemetrySimulatorService.evaluateThresholds(machine, 60.0, VIB_CRITICAL);

        verify(workOrderService).openAutoCorrectiveOrder(eq(machine), anyString());
        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void temperatureAtWarningThreshold_raisesWarningAlertWithoutWorkOrder() {
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        telemetrySimulatorService.evaluateThresholds(machine, TEMP_WARNING, 2.5);

        verify(workOrderService, never()).openAutoCorrectiveOrder(any(), anyString());
        verify(alertRepository).save(alertCaptor.capture());
        Alert saved = alertCaptor.getValue();
        assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(saved.getLinkedWorkOrder()).isNull();

        verify(messagingTemplate).convertAndSend(eq("/topic/alerts"), alertMessageCaptor.capture());
        assertThat(alertMessageCaptor.getValue().linkedWorkOrderId()).isNull();
    }

    @Test
    void vibrationAtWarningThreshold_raisesWarningAlertWithoutWorkOrder() {
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        telemetrySimulatorService.evaluateThresholds(machine, 60.0, VIB_WARNING);

        verify(workOrderService, never()).openAutoCorrectiveOrder(any(), anyString());
        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getSeverity()).isEqualTo(AlertSeverity.WARNING);
    }

    @Test
    void justBelowWarningThreshold_raisesNoAlert() {
        telemetrySimulatorService.evaluateThresholds(machine, TEMP_WARNING - 0.1, VIB_WARNING - 0.1);

        verifyNoInteractions(alertRepository, workOrderService, messagingTemplate);
    }

    @Test
    void criticalTakesPrecedenceOverWarning_whenBothBreached() {
        when(workOrderService.openAutoCorrectiveOrder(eq(machine), anyString()))
                .thenReturn(WorkOrder.builder().id(1L).build());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        telemetrySimulatorService.evaluateThresholds(machine, TEMP_CRITICAL, VIB_WARNING);

        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }
}

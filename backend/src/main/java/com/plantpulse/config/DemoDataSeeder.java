package com.plantpulse.config;

import com.plantpulse.domain.Machine;
import com.plantpulse.domain.SparePart;
import com.plantpulse.domain.Technician;
import com.plantpulse.domain.enums.MachineStatus;
import com.plantpulse.domain.enums.TechnicianStatus;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.repository.SparePartRepository;
import com.plantpulse.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Populates the in-memory H2 database with a believable small plant so the
 * dashboard has something to show the moment the app starts — no manual
 * data entry needed for a demo or a portfolio screen recording.
 */
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final MachineRepository machineRepository;
    private final TechnicianRepository technicianRepository;
    private final SparePartRepository sparePartRepository;

    @Override
    public void run(String... args) {
        if (machineRepository.count() > 0) {
            return;
        }

        machineRepository.save(Machine.builder()
                .name("CNC Lathe #1").type("CNC Lathe").location("Bay A")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(58.0).baselineVibration(2.2)
                .build());

        machineRepository.save(Machine.builder()
                .name("Hydraulic Press #2").type("Press").location("Bay A")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(64.0).baselineVibration(3.1)
                .build());

        machineRepository.save(Machine.builder()
                .name("Conveyor Line 3").type("Conveyor").location("Bay B")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(48.0).baselineVibration(1.6)
                .build());

        machineRepository.save(Machine.builder()
                .name("Industrial Compressor").type("Compressor").location("Utility Room")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(70.0).baselineVibration(4.0)
                .build());

        technicianRepository.save(Technician.builder()
                .name("Marta Ruiz").specialty("Mechanical").email("marta.ruiz@plantpulse.dev")
                .status(TechnicianStatus.AVAILABLE).build());

        technicianRepository.save(Technician.builder()
                .name("Jon Etxeberria").specialty("Electrical").email("jon.etxeberria@plantpulse.dev")
                .status(TechnicianStatus.AVAILABLE).build());

        sparePartRepository.save(SparePart.builder()
                .name("Hydraulic seal kit").stockQuantity(12).minStockThreshold(5).unitCost(24.50).build());

        sparePartRepository.save(SparePart.builder()
                .name("Drive belt (B-section)").stockQuantity(3).minStockThreshold(4).unitCost(18.90).build());

        sparePartRepository.save(SparePart.builder()
                .name("Bearing 6205-2RS").stockQuantity(20).minStockThreshold(10).unitCost(6.75).build());
    }
}

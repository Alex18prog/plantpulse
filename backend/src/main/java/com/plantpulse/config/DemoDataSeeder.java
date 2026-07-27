package com.plantpulse.config;

import com.plantpulse.domain.Machine;
import com.plantpulse.domain.SparePart;
import com.plantpulse.domain.Technician;
import com.plantpulse.domain.User;
import com.plantpulse.domain.enums.MachineStatus;
import com.plantpulse.domain.enums.Role;
import com.plantpulse.domain.enums.TechnicianStatus;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.repository.SparePartRepository;
import com.plantpulse.repository.TechnicianRepository;
import com.plantpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (machineRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        // installDate/maintenanceIntervalDays are deliberately picked so two
        // machines are already overdue for preventive maintenance and two
        // aren't — a reviewer sees PreventiveMaintenanceScheduler fire within
        // its first tick without having to wait or seed data manually.
        machineRepository.save(Machine.builder()
                .name("CNC Lathe #1").type("CNC Lathe").location("Bay A")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(58.0).baselineVibration(2.2)
                .installDate(today.minusDays(180)).maintenanceIntervalDays(90)
                .build());

        machineRepository.save(Machine.builder()
                .name("Hydraulic Press #2").type("Press").location("Bay A")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(64.0).baselineVibration(3.1)
                .installDate(today.minusDays(45)).maintenanceIntervalDays(180)
                .build());

        machineRepository.save(Machine.builder()
                .name("Conveyor Line 3").type("Conveyor").location("Bay B")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(48.0).baselineVibration(1.6)
                .installDate(today.minusDays(400)).maintenanceIntervalDays(120)
                .build());

        machineRepository.save(Machine.builder()
                .name("Industrial Compressor").type("Compressor").location("Utility Room")
                .status(MachineStatus.OPERATIONAL)
                .baselineTemperature(70.0).baselineVibration(4.0)
                .installDate(today.minusDays(10)).maintenanceIntervalDays(60)
                .build());

        Technician marta = technicianRepository.save(Technician.builder()
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

        // Demo login accounts so the login flow can be exercised without manual registration.
        userRepository.save(User.builder()
                .email("admin@plantpulse.dev")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .email("marta.ruiz@plantpulse.dev")
                .passwordHash(passwordEncoder.encode("tech123"))
                .role(Role.TECHNICIAN)
                .technician(marta)
                .build());
    }
}

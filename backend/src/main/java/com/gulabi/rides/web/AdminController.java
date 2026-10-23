
package com.gulabi.rides.web;

import com.gulabi.rides.domain.*;
import com.gulabi.rides.dto.Dto.RideView;
import com.gulabi.rides.repo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RideRepository rides;
    private final DriverRepository drivers;
    private final UserRepository users;
    private final SupportTicketRepository tickets;
    private final PromoCodeRepository promos;

    public record UserAdminView(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt
    ) {}

    public record DriverAdminView(
        Long id,
        Long userId,
        String name,
        String email,
        String vehicle,
        String plate,
        boolean online,
        Double lat,
        Double lng,
        Double ratingAvg,
        Integer ratingCount
    ) {}

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        var all = rides.findAll();

        BigDecimal revenue = all.stream()
            .filter(r -> r.getStatus() == RideStatus.PAID)
            .map(Ride::getFare)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long active = all.stream()
            .filter(r -> r.getStatus() == RideStatus.ACCEPTED
                || r.getStatus() == RideStatus.ARRIVED
                || r.getStatus() == RideStatus.IN_TRIP)
            .count();

        long available = all.stream()
            .filter(r -> r.getStatus() == RideStatus.REQUESTED
                && r.getDriver() == null)
            .count();

        return Map.of(
            "rides", all.size(),
            "activeRides", active,
            "availableRides", available,
            "driversOnline", drivers.countByOnlineTrue(),
            "registeredDrivers", drivers.count(),
            "registeredUsers", users.count(),
            "revenue", revenue
        );
    }

    @GetMapping("/users")
    public List<UserAdminView> users() {
        return users.findAll().stream()
            .sorted(Comparator.comparing(
                User::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
            ))
            .map(u -> new UserAdminView(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getCreatedAt()
            ))
            .toList();
    }

    @GetMapping("/drivers")
    public List<DriverAdminView> driverList() {
        return drivers.findAll().stream()
            .map(d -> new DriverAdminView(
                d.getId(),
                d.getUser().getId(),
                d.getUser().getName(),
                d.getUser().getEmail(),
                d.getVehicle(),
                d.getPlate(),
                d.isOnline(),
                d.getLat(),
                d.getLng(),
                d.getRatingAvg(),
                d.getRatingCount()
            ))
            .toList();
    }

    @GetMapping("/rides")
    public List<RideView> recent() {
        return rides.findTop50ByOrderByCreatedAtDesc().stream()
            .map(r -> RideView.of(r, false))
            .toList();
    }

    @GetMapping("/tickets")
    public List<Map<String, Object>> tickets() {
        return tickets.findAll().stream()
            .map(t -> Map.<String, Object>of(
                "id", t.getId(),
                "subject", t.getSubject(),
                "body", t.getBody(),
                "status", t.getStatus(),
                "user", t.getUser().getEmail()
            ))
            .toList();
    }

    @PostMapping("/tickets/{id}/close")
    public void close(@PathVariable Long id) {
        var t = tickets.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        t.setStatus("CLOSED");
        tickets.save(t);
    }

    public record PromoRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9]{3,20}") String code,
        @Min(1) @Max(90) int percentOff,
        @NotNull @DecimalMin("0") BigDecimal maxDiscount,
        boolean active
    ) {}

    @GetMapping("/promos")
    public List<PromoCode> promos() {
        return promos.findAll();
    }

    @PostMapping("/promos")
    public PromoCode savePromo(@Valid @RequestBody PromoRequest q) {
        return promos.save(new PromoCode(
            q.code().toUpperCase(),
            q.percentOff(),
            q.maxDiscount(),
            q.active()
        ));
    }

    @PostMapping("/promos/{code}/toggle")
    public PromoCode toggle(@PathVariable String code) {
        var p = promos.findById(code.toUpperCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        p.setActive(!p.isActive());
        return promos.save(p);
    }
}

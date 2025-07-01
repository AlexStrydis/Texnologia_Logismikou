package com.example.festival;

import com.example.festival.model.Festival;
import com.example.festival.service.FestivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import java.util.List;

@RestController
public class FestivalController {

    private final FestivalService service = new FestivalService();

    @GetMapping("/")
    public String index() {
        return "Welcome to the Festival Management API";
    }

    @PostMapping("/festivals")
    public ResponseEntity<Festival> createFestival(@RequestBody Festival festival) {
        try {
            Festival created = service.createFestival(festival);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/festivals")
    public List<Festival> listFestivals(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                        @RequestParam(required = false) String location) {
        boolean hasFilters = name != null || description != null || startDate != null || endDate != null || location != null;
        if (hasFilters) {
            return service.searchFestivals(name, description, startDate, endDate, location);
        }
        return service.getFestivals();
    }

    @GetMapping("/festivals/{id}")
    public ResponseEntity<Festival> getFestival(@PathVariable Long id) {
        return service.getFestival(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/festivals/{id}")
    public ResponseEntity<Festival> updateFestival(@PathVariable Long id,
                                                   @RequestBody Festival festival) {
        return service.updateFestival(id, festival)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/festivals/{id}")
    public ResponseEntity<Void> deleteFestival(@PathVariable Long id) {
        boolean deleted = service.deleteFestival(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/festivals/{id}/status")
    public ResponseEntity<Festival> setStatus(@PathVariable Long id,
                                              @RequestBody Festival festival) {
        if (festival.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        return service.setStatus(id, festival.getStatus())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

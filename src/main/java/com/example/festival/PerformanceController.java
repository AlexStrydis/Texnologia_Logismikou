package com.example.festival;

import com.example.festival.model.Performance;
import com.example.festival.service.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/festivals/{festivalId}/performances")
public class PerformanceController {

    private final PerformanceService service = new PerformanceService();

    @PostMapping
    public ResponseEntity<Performance> create(@PathVariable Long festivalId,
                                              @RequestBody Performance performance) {
        try {
            Performance created = service.createPerformance(festivalId, performance);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Performance> list(@PathVariable Long festivalId) {
        return service.getPerformances(festivalId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Performance> get(@PathVariable Long id) {
        return service.getPerformance(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

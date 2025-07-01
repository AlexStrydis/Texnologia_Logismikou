package com.example.festival.service;

import com.example.festival.model.Performance;
import com.example.festival.repository.PerformanceRepository;

import java.util.List;
import java.util.Optional;

public class PerformanceService {
    private final PerformanceRepository repository = new PerformanceRepository();

    public Performance createPerformance(Long festivalId, Performance performance) {
        if (performance.getName() == null || performance.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (performance.getDescription() == null || performance.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (performance.getGenre() == null || performance.getGenre().isBlank()) {
            throw new IllegalArgumentException("Genre is required");
        }
        if (performance.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (performance.getBandMembers() == null || performance.getBandMembers().isEmpty()) {
            throw new IllegalArgumentException("At least one band member is required");
        }
        repository.findByNameAndFestival(performance.getName(), festivalId).ifPresent(p -> {
            throw new IllegalArgumentException("Performance name must be unique in the festival");
        });
        performance.setFestivalId(festivalId);
        return repository.save(performance);
    }

    public List<Performance> getPerformances(Long festivalId) {
        return repository.findAllByFestival(festivalId);
    }

    public Optional<Performance> getPerformance(Long id) {
        return repository.findById(id);
    }
}

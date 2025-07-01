package com.example.festival.repository;

import com.example.festival.model.Performance;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PerformanceRepository {
    private final Map<Long, Performance> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Performance save(Performance performance) {
        if (performance.getId() == null) {
            performance.setId(idGenerator.getAndIncrement());
        }
        storage.put(performance.getId(), performance);
        return performance;
    }

    public Optional<Performance> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Performance> findAllByFestival(Long festivalId) {
        return storage.values().stream()
                .filter(p -> Objects.equals(p.getFestivalId(), festivalId))
                .collect(Collectors.toList());
    }

    public Optional<Performance> findByNameAndFestival(String name, Long festivalId) {
        return storage.values().stream()
                .filter(p -> Objects.equals(p.getFestivalId(), festivalId))
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }
}

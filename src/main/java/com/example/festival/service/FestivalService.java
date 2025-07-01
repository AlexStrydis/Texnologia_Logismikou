package com.example.festival.service;

import com.example.festival.model.Festival;
import com.example.festival.model.FestivalStatus;
import com.example.festival.repository.FestivalRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FestivalService {
    private final FestivalRepository repository = new FestivalRepository();

    public Festival createFestival(Festival festival) {
        if (festival.getName() == null || festival.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        repository.findByName(festival.getName()).ifPresent(f -> {
            throw new IllegalArgumentException("Festival name must be unique");
        });
        return repository.save(festival);
    }

    public Optional<Festival> getFestival(Long id) {
        return repository.findById(id);
    }

    public List<Festival> getFestivals() {
        return repository.findAll();
    }

    public List<Festival> searchFestivals(String name, String description,
                                           LocalDate startDate, LocalDate endDate,
                                           String location) {
        List<Festival> festivals = repository.findAll();
        return festivals.stream()
                .filter(f -> name == null || f.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(f -> description == null ||
                        f.getDescription() != null && f.getDescription().toLowerCase().contains(description.toLowerCase()))
                .filter(f -> location == null ||
                        f.getLocation() != null && f.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(f -> startDate == null || (f.getStartDate() != null && !f.getStartDate().isBefore(startDate)))
                .filter(f -> endDate == null || (f.getEndDate() != null && !f.getEndDate().isAfter(endDate)))
                .sorted(Comparator.comparing(Festival::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Festival::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    public Optional<Festival> updateFestival(Long id, Festival data) {
        Optional<Festival> existing = repository.findById(id);
        existing.ifPresent(f -> {
            if (data.getName() != null) {
                f.setName(data.getName());
            }
            if (data.getDescription() != null) {
                f.setDescription(data.getDescription());
            }
            if (data.getStartDate() != null) {
                f.setStartDate(data.getStartDate());
            }
            if (data.getEndDate() != null) {
                f.setEndDate(data.getEndDate());
            }
            if (data.getLocation() != null) {
                f.setLocation(data.getLocation());
            }
            repository.save(f);
        });
        return existing;
    }

    public boolean deleteFestival(Long id) {
        return repository.delete(id);
    }

    public Optional<Festival> setStatus(Long id, FestivalStatus status) {
        Optional<Festival> existing = repository.findById(id);
        existing.ifPresent(f -> {
            f.setStatus(status);
            repository.save(f);
        });
        return existing;
    }
}

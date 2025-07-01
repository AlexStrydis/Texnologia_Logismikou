package com.example.festival.repository;

import com.example.festival.model.Festival;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FestivalRepository {
    private final Map<Long, Festival> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Festival save(Festival festival) {
        if (festival.getId() == null) {
            festival.setId(idGenerator.getAndIncrement());
        }
        storage.put(festival.getId(), festival);
        return festival;
    }

    public Optional<Festival> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Festival> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Festival> findByName(String name) {
        return storage.values().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }
}

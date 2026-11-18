package com.minicommerce.repository.impl;

import com.minicommerce.entity.User;
import com.minicommerce.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    // [Session 6] Map thread-safe.
    // [Session 5 - Map] Map<id, User>.
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(String id) { return Optional.ofNullable(users.get(id)); }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public void save(User user) { users.put(user.getId(), user); }
}

package com.minicommerce.repository;

import com.minicommerce.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    void save(User user);
}

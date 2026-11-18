package com.minicommerce.service.impl;

import com.minicommerce.entity.User;
import com.minicommerce.entity.enums.UserRole;
import com.minicommerce.repository.UserRepository;
import com.minicommerce.service.AuthService;
import com.minicommerce.util.IdGenerator;

public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(String username, String password, UserRole role) {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new IllegalArgumentException("Username and password are required.");
        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("Username already exists.");
        User user = new User(IdGenerator.nextId("USR"), username, password,
                role == null ? UserRole.CUSTOMER : role);
        userRepository.save(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!user.getPassword().equals(password))
            throw new IllegalArgumentException("Invalid username or password.");
        return user;
    }
}

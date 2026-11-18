package com.minicommerce.service;

import com.minicommerce.entity.User;
import com.minicommerce.entity.enums.UserRole;

public interface AuthService {
    User register(String username, String password, UserRole role);
    User login(String username, String password);
}

package io.github.manojohnsons.financeapi.service;

import io.github.manojohnsons.financeapi.domain.model.User;

public interface TokenService {
    
    String generateToken(User user);
}

package com.example.scheduler.service.impl;

import com.example.scheduler.dto.login.LoginRequest;
import com.example.scheduler.dto.login.LoginResponse;
import com.example.scheduler.entity.Account;
import com.example.scheduler.exception.UnauthorizedException;
import com.example.scheduler.repository.AccountRepository;
import com.example.scheduler.security.JwtUtil;
import com.example.scheduler.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        return new LoginResponse(jwtUtil.generate(account.getId(), account.getRole().getName().name()));
    }
}

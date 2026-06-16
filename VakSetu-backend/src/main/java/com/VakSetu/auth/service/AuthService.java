package com.vaksetu.auth.service;

import com.vaksetu.auth.dto.AuthResponse;
import com.vaksetu.auth.dto.LoginRequest;
import com.vaksetu.auth.dto.RefreshTokenRequest;
import com.vaksetu.auth.dto.RegisterRequest;
import com.vaksetu.auth.entity.RefreshToken;
import com.vaksetu.auth.repository.RefreshTokenRepository;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import com.vaksetu.security.JwtTokenProvider;
import com.vaksetu.user.entity.User;
import com.vaksetu.user.entity.UserSkill;
import com.vaksetu.user.repository.UserRepository;
import com.vaksetu.user.repository.UserSkillRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .overallScore(50.0)
                .reputation(50)
                .rank(Rank.CONVERSATIONALIST)
                .role(Role.USER)
                .totalStars(0)
                .topContributorFinishes(0)
                .highestSessionStars(0)
                .sessionsCompleted(0)
                .debatesCompleted(0)
                .roleplaysCompleted(0)
                .gdSessionsJoined(0)
                .build();

        User savedUser = userRepository.save(user);

        UserSkill userSkill = UserSkill.builder()
                .user(savedUser)
                .fluency(50.0)
                .pronunciation(50.0)
                .grammar(50.0)
                .confidence(50.0)
                .empathy(50.0)
                .listening(50.0)
                .engagement(50.0)
                .build();

        userSkillRepository.save(userSkill);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

        saveOrUpdateRefreshToken(savedUser, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        saveOrUpdateRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        if (Boolean.TRUE.equals(storedRefreshToken.getRevoked())
                || !jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = storedRefreshToken.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        storedRefreshToken.setToken(refreshToken);
        storedRefreshToken.setRevoked(false);
        storedRefreshToken.setExpiryDate(calculateRefreshTokenExpiryDate());
        refreshTokenRepository.save(storedRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    private void saveOrUpdateRefreshToken(User user, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .build());

        token.setToken(refreshToken);
        token.setRevoked(false);
        token.setExpiryDate(calculateRefreshTokenExpiryDate());

        refreshTokenRepository.save(token);
    }

    private LocalDateTime calculateRefreshTokenExpiryDate() {
        return LocalDateTime.now().plusDays(30);
    }
}

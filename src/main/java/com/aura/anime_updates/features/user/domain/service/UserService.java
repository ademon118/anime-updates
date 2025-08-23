package com.aura.anime_updates.features.user.domain.service;

import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(String username, String rawPassword) {

        if (userRepository.findByUserName(username).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User newUser = User.builder()
                .userName(username)
                .password(passwordEncoder.encode(rawPassword))
                .build();
        
        return userRepository.save(newUser);
    }
    
    public boolean userExists(String username) {
        return userRepository.findByUserName(username).isPresent();
    }
}
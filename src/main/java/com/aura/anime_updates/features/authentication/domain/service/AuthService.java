package com.aura.anime_updates.features.authentication.domain.service;


import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.authentication.api.request.AuthRequest;
import com.aura.anime_updates.features.authentication.api.response.AuthResponse;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import com.aura.anime_updates.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request){
        if(userRepository.findByUserName(request.getUserName()).isPresent()){
//            return new AuthResponse(false, "Username already exists");
            return AuthResponse.builder()
                    .success(false)
                    .message("Username is already in use")
                    .build();
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
//        return new AuthResponse(true, "User registered successfully");
        return AuthResponse.builder()
                .success(true)
                .message("User registered successfully!")
                .build();
    }

    public AuthResponse login(AuthRequest request){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(),request.getPassword())
            );

            User user = userRepository.findByUserName(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Credentials"));

            String token = jwtUtil.generateToken(user.getId().toString(), user.getUserName());
//            return new AuthResponse(true, "Login successful", token);
            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .build();
        }catch (AuthenticationException e){
//            return new AuthResponse(false, "Invalid username or password");
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid Credentials")
                    .build();
        } catch (Exception e) {
//            return new AuthResponse(false, "An error occurred during login");
            return AuthResponse.builder()
                    .success(false)
                    .message("An error occured during login")
                    .build();
        }
    }
}

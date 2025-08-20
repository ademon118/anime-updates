package com.aura.anime_updates.services;


import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.dto.AuthRequest;
import com.aura.anime_updates.dto.AuthResponse;
import com.aura.anime_updates.repository.UserRepository;
import com.aura.anime_updates.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request){
        if(userRepository.findByUserName(request.getUserName()).isPresent()){
            return new AuthResponse(false, "Username already exists");
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new AuthResponse(true, "User registered successfully");
    }

    public AuthResponse login(AuthRequest request){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(),request.getPassword())
            );

            User user = userRepository.findByUserName(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Credentials"));

            String token = jwtUtil.generateToken(user.getId().toString(), user.getUserName());
            return new AuthResponse(true, "Login successful", token);
        }catch (AuthenticationException e){
            return new AuthResponse(false, "Invalid username or password");
        } catch (Exception e) {
            System.out.println(e);
            return new AuthResponse(false, "An error occurred during login");
        }
    }
}

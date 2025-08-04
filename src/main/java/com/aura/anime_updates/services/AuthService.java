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

    public String register(AuthRequest request){
        if(userRepository.findByUserName(request.getUserName()).isPresent()){
            return "UserName already exists";
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(AuthRequest request){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(),request.getPassword())
            );
            String token = jwtUtil.generateToken(request.getUserName());
            return new AuthResponse(token);
        }catch (AuthenticationException e){
            throw new RuntimeException("Invalid username or password");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}

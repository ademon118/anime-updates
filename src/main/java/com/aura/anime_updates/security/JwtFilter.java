package com.aura.anime_updates.security;

import com.aura.anime_updates.features.authentication.domain.entity.RefreshToken;
import com.aura.anime_updates.features.authentication.domain.repository.RefreshTokenRepository;
import com.aura.anime_updates.features.authentication.domain.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = jwtService.parse(token);
                if (jwtService.isAccess(jws)) {
                    String linkedRefreshJti = jwtService.getRefreshJtiFromAccess(jws);
                    if (linkedRefreshJti == null) {
                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                    RefreshToken rt = refreshTokenRepository.findByJti(linkedRefreshJti).orElse(null);
                    if (rt == null || !rt.isActive()) {
                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                    Long userId = jwtService.getUserId(jws);
                    String username = (String) jws.getPayload().get("username");

                    CustomUserDetails currentUser = new CustomUserDetails(userId, username, "", Collections.emptyList());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException e) {
                //TODO: QUICKIE IMPLEMENTATION, ADD PROPER ERROR RESPONSE HANDLING FOR AUTH
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(req, res);
    }
}

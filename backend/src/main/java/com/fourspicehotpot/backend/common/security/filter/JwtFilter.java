package com.fourspicehotpot.backend.common.security.filter;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/customer/public/")
                || path.startsWith("/api/customer/auth/")
                || path.startsWith("/uploads/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/")
                || path.startsWith("/api/chat/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);

                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if ("CUSTOMER".equals(role)) {
                    String email = jwtUtil.getEmailFromToken(token);
                    org.springframework.security.core.userdetails.User principal =
                            new org.springframework.security.core.userdetails.User(
                                    email,
                                    "",
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } else {
                    var nhanVien = nhanVienRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

                    List<String> permissions = nhanVienRepository.getPermissions(userId);
                    request.setAttribute("permissions", permissions);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    nhanVien.getTaiKhoan(),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception e) {
                e.printStackTrace();
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
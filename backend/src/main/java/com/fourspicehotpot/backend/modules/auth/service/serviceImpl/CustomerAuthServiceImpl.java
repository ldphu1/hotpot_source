package com.fourspicehotpot.backend.modules.auth.service.serviceImpl;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.modules.auth.dto.request.CustomerLoginRequest;
import com.fourspicehotpot.backend.modules.khachhang.mapper.CustomerKhachHangMapper;
import com.fourspicehotpot.backend.modules.auth.service.CustomerAuthService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerAuthServiceImpl implements CustomerAuthService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private CustomerKhachHangMapper khachHangMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(CustomerLoginRequest request) {


        KhachHang kh = khachHangRepository.findByEmail(request.getEmail());

        if (kh == null) {
            throw new RuntimeException("Vui lòng đăng nhập đúng vai trò");
        }

        if (!passwordEncoder.matches(request.getMatKhau(), kh.getMatKhau())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        String token = jwtUtil.generateToken(kh.getId(), "CUSTOMER", kh.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("email", kh.getEmail());
        data.put("name", kh.getTenKhachHang());
        data.put("avatar", kh.getAnhDaiDien());

        return data;


    }

}

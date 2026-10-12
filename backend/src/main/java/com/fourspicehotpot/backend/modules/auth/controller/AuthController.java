package com.fourspicehotpot.backend.modules.auth.controller;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.modules.auth.dto.request.LoginRequest;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        NhanVien nv = nhanVienRepository.findByTaiKhoan(req.getTaiKhoan());

        if (nv == null) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Tài khoản quản trị viên không tồn tại")
            );
        }

        if (!nv.getMatKhau().equals(req.getMatKhau())) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai mật khẩu"));
        }
        String role = nv.getChucVu() == 1 ? "ADMIN" : "NHAN_VIEN";
        String token = jwtUtil.generateToken(nv.getId(), role);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", role,
                "username", nv.getTaiKhoan(),
                "name", nv.getTenNhanVien(),
                "avatar", nv.getAnhNhanVien()
        ));
    }


    @GetMapping("/{id}/permissions")
    public ResponseEntity<?> getPermissions(@PathVariable Long id) {
        List<String> permissions = nhanVienRepository.getPermissions(id);
        return ResponseEntity.ok(permissions);
    }
}

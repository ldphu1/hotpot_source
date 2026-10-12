package com.fourspicehotpot.backend.common.security.oauth2;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    KhachHangRepository khachHangRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        List<KhachHang> list = khachHangRepository.findKhachHangByEmail(email);

        KhachHang khachHang;

        if (list.isEmpty()) {

            KhachHang newKh = new KhachHang();
            newKh.setEmail(email);
            newKh.setTenKhachHang(name);
            newKh.setMatKhau(UUID.randomUUID().toString());
            newKh.setDiaChiList(null);
            newKh.setSoDienThoai(null);

            khachHang = khachHangRepository.save(newKh);

        } else {
            khachHang = list.get(0);
        }
        String token = jwtUtil.generateToken(
                khachHang.getId(),
                "CUSTOMER",
                khachHang.getEmail()
        );
        response.sendRedirect(
                "http://localhost:5173/oauth2/success?token=" + token +
                        "&name=" + java.net.URLEncoder.encode(name, "UTF-8")
        );
    }
}

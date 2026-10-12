package com.fourspicehotpot.backend.modules.chatbot.controller;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.modules.chatbot.service.ChatService;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.GuiTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.TinNhanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final TinNhanService tinNhanService;
    private final JwtUtil jwtUtil;


    @PostMapping("/gui")
    public ResponseEntity<?> guiTinNhan(
            @RequestBody GuiTinNhanRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long idKhachHang = null;
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String role = jwtUtil.getRoleFromToken(jwt);
            if ("CUSTOMER".equals(role)) {
                idKhachHang = jwtUtil.getUserIdFromToken(jwt);
            }
        }

        return ResponseEntity.ok(tinNhanService.guiTinNhan(request, idKhachHang));
    }

    @GetMapping("/tin-nhan/{idCuocTroChuyen}")
    public ResponseEntity<?> getTinNhan(@PathVariable Long idCuocTroChuyen) {
        return ResponseEntity.ok(tinNhanService.getTinNhan(idCuocTroChuyen, null));
    }
}
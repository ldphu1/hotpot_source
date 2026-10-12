package com.fourspicehotpot.backend.modules.cuoctrochuyen.controller;

import com.fourspicehotpot.backend.common.dto.ApiResponseCommon;
import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.infrastructure.constant.MappingConstant;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.ReplyTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.SearchCuocTroChuyenRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.CuocTroChuyenService;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.TinNhanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping(MappingConstant.API_CUOC_TRO_CHUYEN)
public class CuocTroChuyenController {

    private final CuocTroChuyenService cuocTroChuyenService;
    private final TinNhanService tinNhanService;
    private final JwtUtil jwtUtil;

    @GetMapping()
    public ResponseEntity<?> getCuocTroChuyen(@ModelAttribute SearchCuocTroChuyenRequest request) {
        return ResponseEntity.ok(cuocTroChuyenService.getAllCuocTroChuyen(request));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getCuocTroChuyenDetail(@PathVariable Long id, @RequestParam(required = false) LocalDateTime cursor) {
        return ResponseEntity.ok(tinNhanService.getTinNhan(id, cursor));
    }

    @PostMapping("/reply")
    public ApiResponseCommon<ChatRealtimeResponse> replyTinNhan(
            @RequestBody ReplyTinNhanRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long idNhanVien = jwtUtil.getUserIdFromToken(token);
        return cuocTroChuyenService.replyTinNhan(request, idNhanVien);
    }

    @PutMapping("/{id}/nhan-xu-ly")
    public ResponseEntity<ApiResponseCommon<Void>> nhanXuLy(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long idNhanVien = jwtUtil.getUserIdFromToken(token);
        return ResponseEntity.ok(cuocTroChuyenService.nhanXuLy(id, idNhanVien));
    }

    @PutMapping("/{id}/ket-thuc-xu-ly")
    public ResponseEntity<ApiResponseCommon<Void>> ketThucXuLy(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cuocTroChuyenService.ketThucXuLy(id));
    }
}

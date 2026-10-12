package com.fourspicehotpot.backend.modules.checkin.controller;

import com.fourspicehotpot.backend.integration.payment.vnpay.config.VNPayConfig;
import com.fourspicehotpot.backend.integration.payment.vnpay.service.VNPayService;
import com.fourspicehotpot.backend.integration.payment.vnpay.utils.VNPayUtils;
import com.fourspicehotpot.backend.modules.checkin.dto.request.TaoThanhToanRequest;
import com.fourspicehotpot.backend.modules.checkin.service.ThanhToanService;
import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/thanh-toan")
public class ThanhToanController {

    @Autowired
    private ThanhToanService thanhToanService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private HoaDonService hoaDonService;

    // ========================= BASIC =========================

    @GetMapping("/phuong-thuc")
    public ResponseEntity<?> getAllPhuongThucThanhToan() {
        return ResponseEntity.ok(thanhToanService.getAllPhuongThucThanhToan());
    }

    @PostMapping("/tien-mat")
    public ResponseEntity<?> thanhToanTienMat(@RequestBody TaoThanhToanRequest request) {
        return ResponseEntity.ok(thanhToanService.thanhToanTienMat(request));
    }

    @PostMapping("/chuyen-khoan")
    public ResponseEntity<?> thanhToanChuyenKhoan(@RequestBody TaoThanhToanRequest request) {
        return ResponseEntity.ok(thanhToanService.thanhToanChuyenKhoan(request));
    }

    // ========================= VNPAY =========================

    @GetMapping("/vnpay")
    public ResponseEntity<?> createVnPayPayment(
            @RequestParam Long amount,
            @RequestParam String maThanhToan,
            HttpServletRequest request
    ) {
        String paymentUrl = vnPayService.createPaymentUrl(amount, maThanhToan, request);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> fields = VNPayUtils.getParamsFromRequest(request);

        String secureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = VNPayUtils.hmacSHA512(
                vnPayConfig.getHashSecret(),
                VNPayUtils.buildQuery(fields)
        );

        String maThanhToan = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        // check chữ ký
        if (!signValue.equals(secureHash)) {
            response.sendRedirect("http://localhost:5173/thanh-toan/ket-qua?vnp_TxnRef="
                    + maThanhToan + "&vnp_ResponseCode=97");
            return;
        }

        // check thành công
        if ("00".equals(responseCode)) {
            thanhToanService.xacNhanThanhToanThanhCong(maThanhToan);
            response.sendRedirect("http://localhost:5173/thanh-toan/ket-qua?vnp_TxnRef="
                    + maThanhToan + "&vnp_ResponseCode=00");
            return;
        }

        response.sendRedirect("http://localhost:5173/thanh-toan/ket-qua?vnp_TxnRef="
                + maThanhToan + "&vnp_ResponseCode=" + responseCode);
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<?> vnpayIpn(HttpServletRequest request) {

        Map<String, String> fields = VNPayUtils.getParamsFromRequest(request);

        String secureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = VNPayUtils.hmacSHA512(
                vnPayConfig.getHashSecret(),
                VNPayUtils.buildQuery(fields)
        );

        String maThanhToan = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        if (!signValue.equals(secureHash)) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "97",
                    "Message", "Invalid Signature"
            ));
        }

        if ("00".equals(responseCode)) {
            thanhToanService.xacNhanThanhToanThanhCong(maThanhToan);
            return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "RspCode", "99",
                "Message", "Payment Failed"
        ));
    }

    // ========================= HÓA ĐƠN =========================

    @GetMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<?> getDanhSachThanhToanTheoHoaDon(@PathVariable Long idHoaDon) {
        return ResponseEntity.ok(thanhToanService.getDanhSachThanhToanTheoHoaDon(idHoaDon));
    }

    @GetMapping("/chi-tiet-hoa-don/{idHoaDon}")
    public ResponseEntity<?> getChiTietHoaDonThanhToan(@PathVariable Long idHoaDon) {
        return ResponseEntity.ok(thanhToanService.getChiTietHoaDonThanhToan(idHoaDon));
    }

    @GetMapping("/in-hoa-don/{idHoaDon}")
    public ResponseEntity<byte[]> inHoaDon(@PathVariable Long idHoaDon) {
        byte[] pdf = hoaDonService.inHoaDon(idHoaDon);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ========================= ACTION =========================

    @PostMapping("/xac-nhan-hoan-tat/{idHoaDon}")
    public ResponseEntity<?> xacNhanHoanTat(@PathVariable Long idHoaDon) {
        thanhToanService.xacNhanHoanTatHoaDon(idHoaDon);
        return ResponseEntity.ok("Đã chuyển hóa đơn sang hoàn thành");
    }

}
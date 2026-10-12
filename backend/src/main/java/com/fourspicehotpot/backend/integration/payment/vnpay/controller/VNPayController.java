package com.fourspicehotpot.backend.integration.payment.vnpay.controller;

import com.fourspicehotpot.backend.common.enums.TrangThaiDatBan;
import com.fourspicehotpot.backend.common.enums.TrangThaiHoaDon;
import com.fourspicehotpot.backend.integration.payment.vnpay.config.VNPayConfig;
import com.fourspicehotpot.backend.integration.payment.vnpay.service.VNPayService;
import com.fourspicehotpot.backend.integration.payment.vnpay.utils.VNPayUtils;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/api/payment")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private VNPayConfig config;

    @Autowired
    private DatBanRepository datBanRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @Autowired
    private WebSocketService webSocketService;

    @GetMapping("/vnpay")
    public String createPayment(
            @RequestParam Long amount,
            @RequestParam String orderId,
            HttpServletRequest request) {

        return vnPayService.createPaymentUrl(amount, orderId, request);
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request) {

        Map<String, String> fields = VNPayUtils.getParamsFromRequest(request);

        String secureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = VNPayUtils.hmacSHA512(
                config.getHashSecret(),
                VNPayUtils.buildQuery(fields)
        );

        if (!signValue.equals(secureHash)) {
            return "Sai chữ ký";
        }
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderId = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");


        DatBan datBan = datBanRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

        if ("00".equals(responseCode)) {

            if (datBan.getTrangThaiCoc() != TrangThaiDatBan.DA_COC.getCode()) {
                datBan.setTrangThaiCoc(TrangThaiDatBan.DA_COC.getCode());
                datBanRepository.save(datBan);

                HoaDon hoaDon = hoaDonRepository.findByDatBanId(datBan.getId()).orElse(null);
                if (hoaDon != null) {
                    hoaDon.setTrangThai(TrangThaiHoaDon.DA_COC.getCode());
                    hoaDonRepository.save(hoaDon);

                    lichSuHoaDonService.saveLichSuHoaDon(
                            hoaDon,
                            "THANH_TOAN_COC",
                            1,
                            "Thanh toán cọc VNPay thành công - Mã GD: " + transactionNo
                    );
                }
                webSocketService.sendDatBanUpdate(datBan);
                webSocketService.sendHoaDonUpdate(hoaDon);
            }
            return "redirect:http://localhost:5173/payment-success?orderId=" + orderId
                    + "&status=success";

        } else {
            return "redirect:http://localhost:5173/payment-fail?orderId=" + orderId;
        }
    }
}
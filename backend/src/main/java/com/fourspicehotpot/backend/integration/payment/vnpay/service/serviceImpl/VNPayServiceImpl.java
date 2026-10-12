package com.fourspicehotpot.backend.integration.payment.vnpay.service.serviceImpl;

import com.fourspicehotpot.backend.integration.payment.vnpay.config.VNPayConfig;
import com.fourspicehotpot.backend.integration.payment.vnpay.service.VNPayService;
import com.fourspicehotpot.backend.integration.payment.vnpay.utils.VNPayUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Service
public class VNPayServiceImpl implements VNPayService {

    @Autowired
    private VNPayConfig config;

    @Override
    public String createPaymentUrl(Long amount, String orderId, HttpServletRequest request) {


        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = config.getTmnCode();
        String vnp_IpAddr = "8.8.8.8";
        String vnp_TxnRef = orderId;
        String vnp_OrderInfo = "Thanh toan coc dat ban " + orderId;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";
        System.out.println("IP: " + request.getRemoteAddr());
        System.out.println("ReturnUrl: " + config.getReturnUrl());
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

        Calendar cld = Calendar.getInstance(tz);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);

        String vnp_CreateDate = formatter.format(cld.getTime());

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        System.out.println("===== VNPAY DEBUG =====");
        System.out.println("Current time: " + formatter.format(Calendar.getInstance().getTime()));
        System.out.println("CreateDate: " + vnp_CreateDate);
        System.out.println("ExpireDate: " + vnp_ExpireDate);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", vnp_CurrCode);

        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", vnp_OrderInfo);
        params.put("vnp_OrderType", vnp_OrderType);

        params.put("vnp_Locale", vnp_Locale);
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", vnp_IpAddr);

        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_ExpireDate", vnp_ExpireDate);

        String query = VNPayUtils.buildQuery(params);


        System.out.println("Query: " + query);

        String secureHash = VNPayUtils.hmacSHA512(config.getHashSecret(), query);
        System.out.println("SecureHash: " + secureHash);

        System.out.println("SecureHash: " + secureHash);

        String fullUrl = config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        System.out.println("Payment URL: " + fullUrl);
        System.out.println("=======================");

        return fullUrl;

    }

    // ===== THANH TOÁN HÓA ĐƠN =====
    @Override
    public String createInvoicePaymentUrl(Long amount, String maThanhToan, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = config.getTmnCode();
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_TxnRef = maThanhToan;
        String vnp_OrderInfo = "Thanh toan hoa don " + maThanhToan;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";

        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(tz);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);

        String vnp_CreateDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", vnp_CurrCode);
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", vnp_OrderInfo);
        params.put("vnp_OrderType", vnp_OrderType);
        params.put("vnp_Locale", vnp_Locale);
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", vnp_IpAddr);
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_ExpireDate", vnp_ExpireDate);

        String query = VNPayUtils.buildQuery(params);
        String secureHash = VNPayUtils.hmacSHA512(config.getHashSecret(), query);

        return config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }
}

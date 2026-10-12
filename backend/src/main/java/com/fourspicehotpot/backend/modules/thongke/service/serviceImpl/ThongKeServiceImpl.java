package com.fourspicehotpot.backend.modules.thongke.service.serviceImpl;

import com.fourspicehotpot.backend.modules.thongke.dto.response.*;
import com.fourspicehotpot.backend.modules.thongke.repository.ThongKeRepository;
import com.fourspicehotpot.backend.modules.thongke.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThongKeServiceImpl implements ThongKeService {
    @Autowired
    private ThongKeRepository thongKeRepository;

    @Override
    public ThongKeDoanhThuResponse layThongKeDoanhThu() {

        BigDecimal homNay = thongKeRepository.getDoanhThuThucThuHomNay();
        BigDecimal duKienHomNay = thongKeRepository.getDoanhThuDuKienHomNay();
        BigDecimal homQua = thongKeRepository.getDoanhThuThucThuHomQua();

        BigDecimal tuanNay = thongKeRepository.getDoanhThuThucThuTuan();
        BigDecimal duKienTuan = thongKeRepository.getDoanhThuDuKienTuan();
        BigDecimal tuanTruoc = thongKeRepository.getDoanhThuTuanTruoc();

        BigDecimal thangNay = thongKeRepository.getDoanhThuThucThuThang();
        BigDecimal duKienThang = thongKeRepository.getDoanhThuDuKienThang();
        BigDecimal thangTruoc = thongKeRepository.getDoanhThuThangTruoc();

        BigDecimal namNay = thongKeRepository.getDoanhThuThucThuNam();
        BigDecimal duKienNam = thongKeRepository.getDoanhThuDuKienNam();
        BigDecimal namTruoc = thongKeRepository.getDoanhThuNamTruoc();

        return new ThongKeDoanhThuResponse(
                homNay, duKienHomNay, homQua,
                tuanNay, duKienTuan, tuanTruoc,
                thangNay, duKienThang, thangTruoc,
                namNay, duKienNam, namTruoc
        );
    }

    @Override
    public ThongKeTongQuanResponse layThongKeTongQuan(LocalDateTime from, LocalDateTime to) {

        Object[] wrapper = thongKeRepository.thongKeTongQuan(from, to);

        Object[] rs = (Object[]) wrapper[0];
        System.out.println(wrapper.getClass());
        System.out.println(wrapper[0].getClass());
        return ThongKeTongQuanResponse.builder()
                .tongHoaDon(((Number) rs[0]).longValue())
                .trungBinhDon(rs[1] != null ? new BigDecimal(rs[1].toString()) : BigDecimal.ZERO)
                .tienCoc((BigDecimal) rs[2])
                .giamGia((BigDecimal) rs[3])
                .huyDon(((Number) rs[4]).longValue())
                .thucNhan((BigDecimal) rs[5])
                .build();
    }

    @Override
    public List<DoanhThuTheoThangResponse> thongKeDoanhThuTheoThang() {
        List<Object[]> rs = thongKeRepository.thongKeDoanhThuTheoThang();

        return rs.stream().map(o -> DoanhThuTheoThangResponse.builder()
                .thang(((Number) o[0]).intValue())
                .doanhThu(o[1] != null ? new BigDecimal(o[1].toString()) : BigDecimal.ZERO)
                .build()
        ).toList();
    }

    @Override
    public List<TopSetResponse> getTopSet() {
        List<Object[]> rs = thongKeRepository.getTopSet();

        return rs.stream().map(o -> TopSetResponse.builder()
                .ten(o[0] != null ? o[0].toString() : "")
                .gia(o[1] != null ? new BigDecimal(o[1].toString()) : BigDecimal.ZERO)
                .hinhAnh(o[2] != null ? o[2].toString() : "")
                .ban(o[3] != null ? ((Number) o[3]).longValue() : 0L)
                .build()
        ).toList();
    }

    @Override
    public BookingChannelResponse getBookingChannel() {
        List<Object[]> results = thongKeRepository.getBookingChannel();

        if (results != null && !results.isEmpty()) {
            Object[] rs = results.get(0);

            int online = rs[0] != null ? ((Number) rs[0]).intValue() : 0;
            int taiQuay = rs[1] != null ? ((Number) rs[1]).intValue() : 0;

            return BookingChannelResponse.builder()
                    .online(online)
                    .taiQuay(taiQuay)
                    .build();
        }

        return BookingChannelResponse.builder().online(0).taiQuay(0).build();
    }

    @Override
    public TrangThaiResponse getTrangThai() {
        Object[] rs = thongKeRepository.thongKeTrangThai().get(0);

        return TrangThaiResponse.builder()
                .vuaTao(((Number) rs[0]).intValue())
                .choXacNhan(((Number) rs[1]).intValue())
                .xacNhan(((Number) rs[2]).intValue())
                .phucVu(((Number) rs[3]).intValue())
                .choThanhToan(((Number) rs[4]).intValue())
                .daThanhToan(((Number) rs[5]).intValue())
                .hoanThanh(((Number) rs[6]).intValue())
                .huy(((Number) rs[7]).intValue())
                .build();
    }

}

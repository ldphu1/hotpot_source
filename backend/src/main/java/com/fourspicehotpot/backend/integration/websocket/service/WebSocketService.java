package com.fourspicehotpot.backend.integration.websocket.service;


import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DatBanRepository datBanRepository;
    private final BanAnRepository banAnRepository;

    private DatBanResponse mapToResponseHienThi(DatBan entity) {
        List<Integer> listIdBan = null;
        List<String> listTenBan = null;
        String tenKhuVuc = null;

        if (entity.getBanAns() != null && !entity.getBanAns().isEmpty()) {

            listIdBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getId)
                    .toList();

            listTenBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getTenBanAn)
                    .toList();


            BanAn first = entity.getBanAns().get(0);
            if (first.getKhuVuc() != null) {
                tenKhuVuc = first.getKhuVuc().getTenKhuVuc();
            }
        }

        return DatBanResponse.builder()
                .id(Math.toIntExact(entity.getId()))
                .maDatBan(entity.getMaDatBan())

                .idKhachHang(entity.getKhachHang().getId())
                .maKhachHang(entity.getKhachHang().getMaKhachHang())
                .tenKhachHang(entity.getKhachHang().getTenKhachHang())
                .soDienThoai(entity.getKhachHang().getSoDienThoai())

                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .tenKhuVuc(tenKhuVuc)

                .tienCoc(entity.getTienCoc())
                .phanTramCoc(entity.getPhanTramCoc())
                .hanGiuBan(entity.getHanGiuBan())
                .hanThanhToanCoc(entity.getHanThanhToanCoc())
                .trangThaiCoc(entity.getTrangThaiCoc())

                .thoiGianDat(entity.getThoiGianDat())
                .soNguoiToiDa(entity.getSoNguoiToiDa())
                .ghiChu(entity.getGhiChu())
                .hinhThucDat(entity.getHinhThucDat())
                .trangThai(entity.getTrangThai())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .build();
    }


    private HoaDonResponse mapToResponse(HoaDon hoaDon) {

        HoaDonResponse res = new HoaDonResponse();

        res.setId(hoaDon.getId());
        res.setMaHoaDon(hoaDon.getMaHoaDon());
        res.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        res.setThoiGianTao(hoaDon.getThoiGianTao());
        res.setThoiGianThanhToan(hoaDon.getThoiGianThanhToan());
        res.setTienCoc(hoaDon.getTienCoc());
        res.setTienThue(hoaDon.getTienThue());
        res.setTenNguoiNhan(hoaDon.getTenNguoiNhan());
        res.setSoDienThoai(hoaDon.getSoDienThoai());
        res.setDiaChiGiao(hoaDon.getDiaChiGiao());
        res.setPhiVanChuyen(hoaDon.getPhiVanChuyen());
        res.setSoTienDuocGiam(hoaDon.getSoTienDuocGiam());
        res.setTongTienMon(hoaDon.getTongTienMon());
        res.setTienSauGiam(hoaDon.getTienSauGiam());
        res.setVatPercent(hoaDon.getVatPercent());
        res.setTienConLai(hoaDon.getTienConLai());
        res.setTongTien(hoaDon.getTongTien());
        res.setTrangThai(hoaDon.getTrangThai());
        res.setGhiChu(hoaDon.getGhiChu());
        res.setNguoiTao(hoaDon.getNguoiTao());
        res.setNgayCapNhat(hoaDon.getNgayCapNhat());
        res.setThoiGianMo(hoaDon.getThoiGianMo());
        res.setThoiGianDong(hoaDon.getThoiGianDong());


        if (hoaDon.getKhachHang() != null) {
            res.setIdKhachHang(hoaDon.getKhachHang().getId());
            res.setTenKhachHang(hoaDon.getKhachHang().getTenKhachHang());
            res.setSoDienThoai(hoaDon.getKhachHang().getSoDienThoai());
        }

        if (hoaDon.getNhanVien() != null) {
            res.setIdNhanVien(hoaDon.getNhanVien().getId());
            res.setTenNhanVien(hoaDon.getNhanVien().getTenNhanVien());
        }

        if (hoaDon.getBanAns() != null && !hoaDon.getBanAns().isEmpty()) {

            List<Long> ids = hoaDon.getBanAns()
                    .stream()
                    .map(b -> Long.valueOf(b.getId()))
                    .toList();

            List<String> tens = hoaDon.getBanAns()
                    .stream()
                    .map(BanAn::getTenBanAn)
                    .toList();

            res.setListIdBan(ids);
            res.setListTenBan(tens);

            BanAn first = hoaDon.getBanAns().get(0);
            if (first.getKhuVuc() != null) {
                res.setTenTang(first.getKhuVuc().getTenKhuVuc());
            }
        }

        if (hoaDon.getDatBan() != null) {
            res.setIdDatBan(Long.valueOf(hoaDon.getDatBan().getId()));
            res.setHinhThucDat(hoaDon.getDatBan().getHinhThucDat());
        }

        if (hoaDon.getPhieuGiamGia() != null) {
            res.setIdPhieuGiamGia((long) hoaDon.getPhieuGiamGia().getId());
        }

        return res;
    }


    public void sendDatBanUpdate(DatBan data) {
        DatBanResponse res = mapToResponseHienThi(data);
        messagingTemplate.convertAndSend("/topic/datban", res);
    }

    public void sendHoaDonUpdate(HoaDon data) {
        HoaDonResponse res = mapToResponse(data);
        messagingTemplate.convertAndSend("/topic/hoadon", res);
    }

    public void sendDatBanDetailUpdate(Long id) {
        DatBan datBan = datBanRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        DatBanResponse res = mapToResponseHienThi(datBan);
        messagingTemplate.convertAndSend("/topic/datban/" + id, res);
    }

    public void sendRealtimeChonBan() {
        List<BanAn> lstBan = banAnRepository.findAll();
        messagingTemplate.convertAndSend("/topic/banan", lstBan);
    }

    public void sendMonDaChonUpdate() {
        messagingTemplate.convertAndSend("/topic/mon-da-chon", "update");
        messagingTemplate.convertAndSend("/topic/ban", "update");
    }

    public void sendChatToTopic(String topic, ChatRealtimeResponse data) {
        messagingTemplate.convertAndSend(topic, data);
    }

    public void sendTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }
}
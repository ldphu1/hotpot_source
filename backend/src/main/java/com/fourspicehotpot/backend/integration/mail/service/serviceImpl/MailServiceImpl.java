package com.fourspicehotpot.backend.integration.mail.service.serviceImpl;

import com.fourspicehotpot.backend.integration.mail.service.MailService;
import com.fourspicehotpot.backend.integration.mail.utils.MailTemplateUtil;
import com.fourspicehotpot.backend.integration.mail.utils.SendMail;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private DatBanRepository datBanRepository;

    @Async
    @Override
    public void sendMailXacNhan(Long idDatBan, Long idHoaDon) {

        DatBan db = datBanRepository.findDetailById(idDatBan)
                .orElseThrow();

        HoaDon hoaDon = hoaDonRepository.findDetailById(idHoaDon)
                .orElseThrow();
        KhachHang kh = db.getKhachHang();
        if (kh == null || kh.getEmail() == null || kh.getEmail().isEmpty()) {
            return;
        }

        try {

            String html = MailTemplateUtil.loadTemplate("mail-dat-ban.html");
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            String monSection = "";

            if (hoaDon != null
                    && hoaDon.getHoaDonChiTiets() != null
                    && !hoaDon.getHoaDonChiTiets().isEmpty()) {

                StringBuilder monHtml = new StringBuilder();

                // Tạo tiêu đề Section và Khung Card bao bọc danh sách món ăn
                monHtml.append("<div style='margin-top: 25px; border-left: 4px solid #8c0000; padding-left: 15px;'>")
                        .append("  <div style='font-size: 13px; font-weight: bold; color: #8c0000; margin-bottom: 10px; text-transform: uppercase;'>Món đã đặt trước</div>")
                        .append("  <div style='background-color: #fafafa; border: 1px solid #eeeeee; border-radius: 8px; padding: 15px;'>")
                        .append("    <table width='100%' border='0' cellspacing='0' cellpadding='0' style='font-size: 13px;'>");

                for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                    String tenMon = "Món không xác định";
                    if (ct.getSanPham() != null) {
                        tenMon = ct.getSanPham().getTenSanPham();
                    }

                    int soLuong = ct.getSoLuong() != null ? ct.getSoLuong() : 0;
                    double donGia = ct.getDonGia() != null ? ct.getDonGia().doubleValue() : 0;
                    double thanhTien = soLuong * donGia;

                    // Mỗi món ăn được phân chia hàng gọn gàng, có đường kẻ đứt nhẹ giữa các món
                    monHtml.append("<tr>")
                            .append("  <td style='padding: 8px 0; color: #282828;'><strong>").append(tenMon).append("</strong></td>")
                            .append("  <td style='padding: 8px 0; text-align: right; color: #646464;'>x").append(soLuong).append("</td>")
                            .append("</tr>")
                            .append("<tr>")
                            .append("  <td colspan='2' style='padding-bottom: 8px; text-align: right; color: #8c0000; font-weight: bold; border-bottom: 1px dashed #eeeeee;'>")
                            .append(nf.format(thanhTien)).append(" ₫")
                            .append("  </td>")
                            .append("</tr>");
                }

                // Loại bỏ border-bottom dashed thừa của dòng cuối cùng bằng cách đóng table
                monHtml.append("    </table>")
                        .append("  </div>")
                        .append("</div>");

                monSection = monHtml.toString();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String tenBan = "Chưa xếp bàn";

            if (db.getBanAns() != null && !db.getBanAns().isEmpty()) {
                tenBan = db.getBanAns().stream()
                        .map(BanAn::getTenBanAn)
                        .collect(Collectors.joining(", "));
            }
            html = html
                    .replace("{{ TEN_KHACH }}", kh.getTenKhachHang())
                    .replace("{{ MA_DAT_BAN }}", db.getMaDatBan() != null ? db.getMaDatBan() : String.valueOf(db.getId()))
                    .replace("{{ SO_DIEN_THOAI }}", kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "Chưa cập nhật")
                    .replace("{{ BAN_AN }}", tenBan)
                    .replace("{{ THOI_GIAN }}",
                            db.getThoiGianDat() != null
                                    ? db.getThoiGianDat().format(formatter)
                                    : "Không rõ")
                    .replace("{{ SO_NGUOI }}",
                            db.getSoNguoiToiDa() != null
                                    ? db.getSoNguoiToiDa().toString()
                                    : "0")
                    .replace("{{ TIEN_COC }}",
                            hoaDon != null && hoaDon.getTienCoc() != null
                                    ? nf.format(hoaDon.getTienCoc()) + " ₫"
                                    : "0 ₫")
                    .replace("{{ GHI_CHU }}",
                            db.getGhiChu() != null && !db.getGhiChu().trim().isEmpty() ? db.getGhiChu() : "Không có")
                    .replace("{{ MON_SECTION }}", monSection);

            html = html.replaceAll("\\r\\n|\\r|\\n", " ");
            SendMail.sendMail(
                    kh.getEmail(),
                    "Xác nhận đặt bàn thành công - FourSpice Hotpot 🍲",
                    html
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMailHuyDoQuaHanCoc(Long idDatBan, Long idHoaDon) {
        DatBan db = datBanRepository.findDetailById(idDatBan)
                .orElseThrow();
        HoaDon hoaDon = hoaDonRepository.findDetailById(idHoaDon)
                .orElseThrow();
        processAndSendMail(db, hoaDon, "huy-do-chua-coc.html", "Đơn đặt bàn của bạn đã bị hủy");

    }

    @Async
    @Override
    public void sendMailDatBan(DatBan db, HoaDon hoaDon) {

        processAndSendMail(db, hoaDon, "mail-cho-xac-nhan.html", "Yêu cầu đặt bàn của bạn đang chờ xác nhận - FourSpice Hotpot ⏳");
    }

    private void processAndSendMail(DatBan db, HoaDon hoaDon, String templateName, String subject) {
        KhachHang kh = db.getKhachHang();
        if (kh == null || kh.getEmail() == null || kh.getEmail().isEmpty()) return;

        try {
            String html = MailTemplateUtil.loadTemplate(templateName);
            String monSection = generateMonSection(hoaDon);

            String tenBan = "Chờ nhân viên xếp bàn";
            if (db.getBanAns() != null && !db.getBanAns().isEmpty()) {
                tenBan = db.getBanAns().stream()
                        .map(BanAn::getTenBanAn)
                        .collect(Collectors.joining(", "));
            }

            html = html
                    .replace("{{ TEN_KHACH }}", kh.getTenKhachHang())
                    .replace("{{ SO_DIEN_THOAI }}", kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "Chưa cập nhật")
                    .replace("{{ BAN_AN }}", tenBan)
                    .replace("{{ THOI_GIAN }}", db.getThoiGianDat() != null ? db.getThoiGianDat().format(formatter) : "Không rõ")
                    .replace("{{ SO_NGUOI }}", db.getSoNguoiToiDa() != null ? db.getSoNguoiToiDa().toString() : "0")
                    .replace("{{ TIEN_COC }}", hoaDon != null && hoaDon.getTienCoc() != null ? nf.format(hoaDon.getTienCoc()) + " ₫" : "0 ₫")
                    .replace("{{ GHI_CHU }}", db.getGhiChu() != null && !db.getGhiChu().trim().isEmpty() ? db.getGhiChu() : "Không có")
                    .replace("{{ MON_SECTION }}", monSection);

            html = html.replaceAll("\\r\\n|\\r|\\n", " ");

            SendMail.sendMail(kh.getEmail(), subject, html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateMonSection(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return "";
        }

        StringBuilder monHtml = new StringBuilder();
        monHtml.append("<div style='margin-top: 25px; border-left: 4px solid #8c0000; padding-left: 15px;'>")
                .append("  <div style='font-size: 13px; font-weight: bold; color: #8c0000; margin-bottom: 10px; text-transform: uppercase;'>Món đã đặt trước</div>")
                .append("  <div style='background-color: #fafafa; border: 1px solid #eeeeee; border-radius: 8px; padding: 15px;'>")
                .append("    <table width='100%' border='0' cellspacing='0' cellpadding='0' style='font-size: 13px;'>");

        for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
            String tenMon = (ct.getSanPham() != null) ? ct.getSanPham().getTenSanPham() : "Món không xác định";
            int soLuong = (ct.getSoLuong() != null) ? ct.getSoLuong() : 0;
            double donGia = (ct.getDonGia() != null) ? ct.getDonGia().doubleValue() : 0;
            double thanhTien = soLuong * donGia;

            monHtml.append("<tr>")
                    .append("  <td style='padding: 8px 0; color: #282828;'><strong>").append(tenMon).append("</strong></td>")
                    .append("  <td style='padding: 8px 0; text-align: right; color: #646464;'>x").append(soLuong).append("</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("  <td colspan='2' style='padding-bottom: 8px; text-align: right; color: #8c0000; font-weight: bold; border-bottom: 1px dashed #eeeeee;'>")
                    .append(nf.format(thanhTien)).append(" ₫")
                    .append("  </td>")
                    .append("</tr>");
        }
        monHtml.append("</table></div></div>");
        return monHtml.toString();
    }

    /// ///////////////////////// PHIÊU GIẢM GIÁ ///////////////////
    @Async
    @Override
    public void sendVoucherMail(PhieuGiamGia entity, KhachHang kh, String subject, String templateName) {
        try {
            String giaTriGiam;
            String giamToiDa;

            if (entity.getPhuongThucGiamGia() == 2) { // Giảm tiền mặt
                giaTriGiam = "GIẢM TRỰC TIẾP " + nf.format(entity.getGiaTriGiam()) + " VNĐ";
                giamToiDa = "Đã trừ vào hóa đơn";
            } else { // Giảm %
                giaTriGiam = "GIẢM NGAY " + entity.getGiaTriGiam() + "%";
                giamToiDa = (entity.getGiamToiDa() != null && entity.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0)
                        ? nf.format(entity.getGiamToiDa()) + " VNĐ"
                        : "Không giới hạn";
            }

            String html = MailTemplateUtil.loadTemplate(templateName);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            html = html
                    .replace("{{ MA_PHIEU }}", entity.getMaGiamGia())
                    .replace("{{ GIA_TRI_GIAM }}", giaTriGiam)
                    .replace("{{ GIAM_TOI_DA }}", giamToiDa)
                    .replace("{{ DIEU_KIEN }}", nf.format(entity.getDieuKienSuDung()) + " VNĐ")
                    .replace("{{ NGAY_BD }}", entity.getNgayBatDau().format(df))
                    .replace("{{ NGAY_KT }}", entity.getNgayKetThuc().format(df));


            SendMail.sendMail(
                    kh.getEmail(),
                    subject,
                    html
            );

        } catch (Exception e) {
            logger.error("Gửi mail voucher thất bại cho KH {}", kh.getEmail(), e);
        }
    }

    @Async
    @Override
    public void sendMailNewVoucher(PhieuGiamGia entity, KhachHang kh) {
        sendVoucherMail(entity, kh,
                "🎉 FourSpiceHotpot gửi bạn ưu đãi đặc biệt",
                "voucher.html"
        );
    }

    @Async
    @Override
    public void sendMailUpdateVoucher(PhieuGiamGia entity, KhachHang kh) {
        sendVoucherMail(entity, kh,
                "🔔 Voucher của bạn vừa được cập nhật",
                "voucher-update.html"
        );
    }

}
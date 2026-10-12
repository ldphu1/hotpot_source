package com.fourspicehotpot.backend.integration.mail.service;

import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;

public interface MailService {
    void sendMailXacNhan(Long idDatBan, Long idHoaDon);

    void sendMailHuyDoQuaHanCoc(Long idDatBan, Long idHoaDon);

    void sendMailDatBan(DatBan db, HoaDon hoaDon);

    /// ///////////////// PHIẾU GIẢM GIÁ ////////////////
    void sendVoucherMail(PhieuGiamGia entity, KhachHang kh, String subject, String templateName);

    void sendMailNewVoucher(PhieuGiamGia entity, KhachHang kh);

    void sendMailUpdateVoucher(PhieuGiamGia entity, KhachHang kh);
}

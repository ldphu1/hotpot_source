package com.fourspicehotpot.backend.integration.pdf.utils;

import com.fourspicehotpot.backend.integration.qrcode.utils.QrCodeUtil;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HoaDonPdf {

    private static final DeviceRgb DEEP_RED = new DeviceRgb(140, 0, 0);
    private static final DeviceRgb TEXT_MAIN = new DeviceRgb(40, 40, 40);
    private static final DeviceRgb TEXT_SUB = new DeviceRgb(100, 100, 100);
    private static final DeviceRgb BG_LIGHT = new DeviceRgb(252, 252, 252);
    private static final DeviceRgb LINE_COLOR = new DeviceRgb(200, 200, 200);

    @Autowired
    private QrCodeUtil qrCodeUtil;

    public byte[] generateHoaDonPdf(HoaDonResponse hoaDon, List<HoaDonChiTietResponse> chiTietList) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(60, 50, 60, 50);

            drawModernBorder(pdf);

            PdfFont font = loadFont();
            document.setFont(font);

            renderHeader(document);

            // 1. Box Thông tin khách hàng
            document.add(new Paragraph("THÔNG TIN HÓA ĐƠN").setBold().setFontSize(10).setFontColor(DEEP_RED).setMarginBottom(5));
            document.add(buildCustomerInfoBox(hoaDon));

            document.add(new Paragraph("").setMarginTop(15));

            // 2. Box Chi tiết dịch vụ (Đã xử lý THẲNG HÀNG)
            document.add(new Paragraph("CHI TIẾT DỊCH VỤ").setBold().setFontSize(10).setFontColor(DEEP_RED).setMarginBottom(5));
            renderUnifiedItemsBox(document, chiTietList);

            // 3. Summary Layout
            document.add(new Paragraph("").setMarginTop(20));
            document.add(buildBottomLayout(hoaDon));

            document.add(new Paragraph("Cảm ơn quý khách và hẹn gặp lại!")
                    .setFontSize(9).setItalic().setFontColor(TEXT_SUB)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(40));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderHeader(Document document) throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/static/logo/logo.png")) {
            if (is != null) {
                Image logo = new Image(ImageDataFactory.create(is.readAllBytes()))
                        .scaleToFit(70, 70).setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
        }
        document.add(new Paragraph("FOURSPICE HOTPOT").setFontSize(22).setBold().setFontColor(DEEP_RED).setCharacterSpacing(4f).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("TRẢI NGHIỆM LẨU THÁI CAO CẤP").setFontSize(8).setBold().setFontColor(TEXT_SUB).setCharacterSpacing(1.5f).setTextAlignment(TextAlignment.CENTER).setMarginTop(-5));
        document.add(new Paragraph("").setBorderBottom(new SolidBorder(DEEP_RED, 1f)).setWidth(80).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginTop(10).setMarginBottom(20));
    }

    private Table buildCustomerInfoBox(HoaDonResponse hoaDon) {
        // Tạo bảng 2 cột với tỷ lệ 1.5 - 1
        Table table = new Table(new float[]{1.5f, 1}).useAllAvailableWidth();
        table.setBorder(new SolidBorder(LINE_COLOR, 0.5f)).setBackgroundColor(BG_LIGHT);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // --- CỘT TRÁI: THÔNG TIN KHÁCH HÀNG (3 DÒNG) ---
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(10);

        // Dòng 1: Tên khách
        left.add(new Paragraph("KHÁCH HÀNG: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(safe(hoaDon.getTenKhachHang(), "KHÁCH LẺ")).setBold().setFontColor(TEXT_MAIN).setFontSize(10)));

        // Dòng 2: Số điện thoại
        left.add(new Paragraph("SỐ ĐIỆN THOẠI: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(safe(hoaDon.getSoDienThoai(), "N/A")).setBold().setFontColor(TEXT_MAIN).setFontSize(10)));

        // Dòng 3: Mã hóa đơn
        left.add(new Paragraph("MÃ HÓA ĐƠN: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(safe(hoaDon.getMaHoaDon(), "---")).setBold().setFontColor(DEEP_RED).setFontSize(10)));

        table.addCell(left);

        // --- CỘT PHẢI: THÔNG TIN THỜI GIAN & BÀN (3 DÒNG - THẲNG HÀNG VỚI BÊN TRÁI) ---
        Cell right = new Cell().setBorder(Border.NO_BORDER).setPadding(10).setTextAlignment(TextAlignment.RIGHT);

        // Dòng 1: Số bàn (Đối xứng với Khách hàng)
        String banText = "BÀN: " + String.join(", ", Optional.ofNullable(hoaDon.getListTenBan()).orElse(List.of()));
        right.add(new Paragraph(banText).setBold().setFontSize(11).setFontColor(DEEP_RED));

        // Dòng 2: Thời gian vào (Đối xứng với SĐT)
        // Giả sử hoaDon có field thoiGianVao, nếu không bạn dùng thoiGianTao tạm nhé
        right.add(new Paragraph("THỜI GIAN VÀO: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(hoaDon.getThoiGianMo().format(fmt)).setBold().setFontColor(TEXT_MAIN).setFontSize(9)));

        // Dòng 3: Thời gian thanh toán (Đối xứng với Mã HD)
        // Dùng thời gian hiện tại lúc xuất file PDF
        right.add(new Paragraph("THỜI GIAN THANH TOÁN: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(java.time.LocalDateTime.now().format(fmt)).setBold().setFontColor(TEXT_MAIN).setFontSize(9)));

        table.addCell(right);

        return table;
    }

    private void renderUnifiedItemsBox(Document document, List<HoaDonChiTietResponse> chiTietList) {
        Map<String, HoaDonChiTietResponse> merged = new LinkedHashMap<>();
        for (HoaDonChiTietResponse item : chiTietList) {
            String key = item.getTenSanPham() + "_" + item.getDonGia();
            merged.merge(key, item, (o, n) -> {
                o.setSoLuong(o.getSoLuong() + n.getSoLuong());
                o.setThanhTien(o.getThanhTien().add(n.getThanhTien()));
                return o;
            });
        }

        Map<String, List<HoaDonChiTietResponse>> grouped = merged.values().stream()
                .collect(Collectors.groupingBy(i -> Optional.ofNullable(i.getTenDanhMuc()).orElse("MÓN KHÁC"), LinkedHashMap::new, Collectors.toList()));

        // Dùng 1 Table duy nhất với tỷ lệ cột cố định để tất cả hàng đều thẳng nhau
        Table mainTable = new Table(new float[]{4, 1, 2, 2}).useAllAvailableWidth();
        mainTable.setBorder(new SolidBorder(LINE_COLOR, 0.5f));

        for (var entry : grouped.entrySet()) {
            // Header danh mục - Chiếm trọn 4 cột
            Cell catHeader = new Cell(1, 4).add(new Paragraph(entry.getKey().toUpperCase())
                            .setBold().setFontSize(9).setFontColor(DEEP_RED))
                    .setBackgroundColor(new DeviceRgb(245, 245, 245))
                    .setPaddingLeft(10).setPaddingTop(5).setPaddingBottom(5)
                    .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(LINE_COLOR, 0.3f));
            mainTable.addCell(catHeader);

            // Các món ăn trong danh mục
            for (HoaDonChiTietResponse item : entry.getValue()) {
                mainTable.addCell(cleanCell(item.getTenSanPham(), TextAlignment.LEFT).setBold().setPaddingLeft(15));
                mainTable.addCell(cleanCell(String.valueOf(item.getSoLuong()), TextAlignment.CENTER));
                mainTable.addCell(cleanCell(format(item.getDonGia()), TextAlignment.RIGHT).setFontColor(TEXT_SUB));
                mainTable.addCell(cleanCell(format(item.getThanhTien()), TextAlignment.RIGHT).setBold().setPaddingRight(10));
            }
        }
        document.add(mainTable);
    }

    private Table buildBottomLayout(HoaDonResponse hoaDon) {
        Table layout = new Table(new float[]{1, 1}).useAllAvailableWidth();

        Cell left = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.TOP);
        try {
            Image qr = qrCodeUtil.generateQr("PAYMENT|" + hoaDon.getMaHoaDon() + "|" + hoaDon.getTongTien()).scaleToFit(80, 80);
            left.add(new Paragraph("QUÉT MÃ THANH TOÁN").setFontSize(7).setBold().setFontColor(TEXT_SUB).setMarginBottom(5));
            left.add(qr);
        } catch (Exception e) {
        }
        layout.addCell(left);

        Table sumTable = new Table(new float[]{1.2f, 1}).useAllAvailableWidth();
        sumTable.setBorder(new SolidBorder(LINE_COLOR, 0.5f)).setBackgroundColor(BG_LIGHT);

        addSummaryRow(sumTable, "Tạm tính hàng hóa:", format(hoaDon.getTongTienMon()));
        addSummaryRow(sumTable, "Voucher / Giảm giá:", "- " + format(hoaDon.getSoTienDuocGiam()));
        addSummaryRow(sumTable, "Tiền đặt cọc:", format(hoaDon.getTienCoc()));

        Cell totalLabel = new Cell().add(new Paragraph("TỔNG THANH TOÁN").setBold().setFontSize(10).setFontColor(DEEP_RED)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(LINE_COLOR, 0.5f)).setPadding(10);
        Cell totalValue = new Cell().add(new Paragraph(format(hoaDon.getTongTien())).setBold().setFontSize(16).setFontColor(DEEP_RED)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(LINE_COLOR, 0.5f)).setTextAlignment(TextAlignment.RIGHT).setPadding(10);

        sumTable.addCell(totalLabel);
        sumTable.addCell(totalValue);

        layout.addCell(new Cell().add(sumTable).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.TOP));

        return layout;
    }

    private void drawModernBorder(PdfDocument pdf) {
        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        canvas.setStrokeColor(DEEP_RED).setLineWidth(1.2f).roundRectangle(25, 25, pdf.getDefaultPageSize().getWidth() - 50, pdf.getDefaultPageSize().getHeight() - 50, 8).stroke();
    }

    private Cell cleanCell(String txt, TextAlignment al) {
        return new Cell().add(new Paragraph(txt).setFontSize(9)).setBorder(Border.NO_BORDER).setPaddingTop(6).setPaddingBottom(6).setBorderBottom(new SolidBorder(LINE_COLOR, 0.1f)).setTextAlignment(al);
    }

    private void addSummaryRow(Table t, String l, String v) {
        t.addCell(new Cell().add(new Paragraph(l).setFontSize(9).setFontColor(TEXT_SUB)).setBorder(Border.NO_BORDER).setPadding(8).setPaddingBottom(2));
        t.addCell(new Cell().add(new Paragraph(v).setFontSize(9).setBold()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setPadding(8).setPaddingBottom(2));
    }

    private PdfFont loadFont() {
        try (InputStream is = getClass().getResourceAsStream("/fonts/Arial.ttf")) {
            return PdfFontFactory.createFont(FontProgramFactory.createFont(is.readAllBytes()), PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String format(Number n) {
        return n == null ? "0 ₫" : String.format("%,.0f ₫", n.doubleValue());
    }

    private String safe(String s, String d) {
        return s != null && !s.isEmpty() ? s : d;
    }
}
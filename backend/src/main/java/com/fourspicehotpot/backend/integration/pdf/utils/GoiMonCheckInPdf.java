package com.fourspicehotpot.backend.integration.pdf.utils;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
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
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class GoiMonCheckInPdf {

    private static final DeviceRgb DEEP_RED = new DeviceRgb(140, 0, 0);
    private static final DeviceRgb TEXT_MAIN = new DeviceRgb(40, 40, 40);
    private static final DeviceRgb TEXT_SUB = new DeviceRgb(100, 100, 100);
    private static final DeviceRgb BG_LIGHT = new DeviceRgb(252, 252, 252);
    private static final DeviceRgb LINE_COLOR = new DeviceRgb(200, 200, 200);

    public byte[] generateGoiMonPdf(HoaDonResponse hoaDon, List<HoaDonChiTiet> chiTietList, Integer soKhach) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            // Set a custom narrower page size (e.g., width 350, height 600)
            pdf.setDefaultPageSize(new com.itextpdf.kernel.geom.PageSize(380f, 650f));
            
            Document document = new Document(pdf);
            document.setMargins(40, 30, 40, 30);

            drawModernBorder(pdf);

            PdfFont font = loadFont();
            document.setFont(font);

            renderHeader(document);

            // Thông tin chung
            document.add(new Paragraph("THÔNG TIN GỌI MÓN").setBold().setFontSize(10).setFontColor(DEEP_RED).setMarginBottom(5));
            document.add(buildInfoBox(hoaDon, soKhach));

            document.add(new Paragraph("").setMarginTop(15));

            // Chi tiết món ăn
            document.add(new Paragraph("CHI TIẾT MÓN ĂN").setBold().setFontSize(10).setFontColor(DEEP_RED).setMarginBottom(5));
            renderItemsBox(document, chiTietList);

            document.add(new Paragraph("Phiếu gửi bếp - vui lòng chuẩn bị món")
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
        document.add(new Paragraph("PHIẾU GỌI MÓN").setFontSize(12).setBold().setFontColor(TEXT_SUB).setCharacterSpacing(1.5f).setTextAlignment(TextAlignment.CENTER).setMarginTop(-5));
        document.add(new Paragraph("").setBorderBottom(new SolidBorder(DEEP_RED, 1f)).setWidth(80).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginTop(10).setMarginBottom(20));
    }

    private Table buildInfoBox(HoaDonResponse hoaDon, Integer soKhach) {
        Table table = new Table(new float[]{1.5f, 1}).useAllAvailableWidth();
        table.setBorder(new SolidBorder(LINE_COLOR, 0.5f)).setBackgroundColor(BG_LIGHT);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        left.add(new Paragraph("MÃ HÓA ĐƠN: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(safe(hoaDon.getMaHoaDon(), "---")).setBold().setFontColor(TEXT_MAIN).setFontSize(10)));
        left.add(new Paragraph("MÃ ĐẶT BÀN: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(hoaDon.getIdDatBan() != null ? "DB" + hoaDon.getIdDatBan() : "---").setBold().setFontColor(TEXT_MAIN).setFontSize(10)));
        left.add(new Paragraph("SỐ KHÁCH: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(soKhach != null ? soKhach + " người" : "---").setBold().setFontColor(TEXT_MAIN).setFontSize(10)));

        table.addCell(left);

        Cell right = new Cell().setBorder(Border.NO_BORDER).setPadding(10).setTextAlignment(TextAlignment.RIGHT);
        String banText = safe(hoaDon.getTenTang(), "---") +
                " - BÀN: " + String.join(", ", Optional.ofNullable(hoaDon.getListTenBan()).orElse(List.of()));
        right.add(new Paragraph(banText).setBold().setFontSize(11).setFontColor(DEEP_RED));
        right.add(new Paragraph("THỜI GIAN VÀO: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(hoaDon.getThoiGianMo() != null ? hoaDon.getThoiGianMo().format(fmt) : "---").setBold().setFontColor(TEXT_MAIN).setFontSize(9)));
        right.add(new Paragraph("THỜI GIAN GỌI: ").setFontSize(8).setFontColor(TEXT_SUB)
                .add(new Paragraph(LocalDateTime.now().format(fmt)).setBold().setFontColor(TEXT_MAIN).setFontSize(9)));

        table.addCell(right);

        return table;
    }

    private void renderItemsBox(Document document, List<HoaDonChiTiet> chiTietList) {
        Table mainTable = new Table(new float[]{4, 1, 4}).useAllAvailableWidth();
        mainTable.setBorder(new SolidBorder(LINE_COLOR, 0.5f));

        Cell h1 = new Cell().add(new Paragraph("TÊN MÓN").setBold().setFontSize(9).setFontColor(TEXT_MAIN)).setBackgroundColor(new DeviceRgb(245, 245, 245)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(LINE_COLOR, 0.5f)).setPadding(5);
        Cell h2 = new Cell().add(new Paragraph("SL").setBold().setFontSize(9).setFontColor(TEXT_MAIN)).setBackgroundColor(new DeviceRgb(245, 245, 245)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(LINE_COLOR, 0.5f)).setTextAlignment(TextAlignment.CENTER).setPadding(5);
        Cell h3 = new Cell().add(new Paragraph("MÔ TẢ").setBold().setFontSize(9).setFontColor(TEXT_MAIN)).setBackgroundColor(new DeviceRgb(245, 245, 245)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(LINE_COLOR, 0.5f)).setTextAlignment(TextAlignment.LEFT).setPadding(5);
        mainTable.addCell(h1); mainTable.addCell(h2); mainTable.addCell(h3);

        for (HoaDonChiTiet item : chiTietList) {
            String tenMon = item.getSanPham() != null ? item.getSanPham().getTenSanPham() : "---";
            String moTa = (item.getSanPham() != null && item.getSanPham().getMoTa() != null && !item.getSanPham().getMoTa().isEmpty()) ? item.getSanPham().getMoTa() : "---";

            mainTable.addCell(cleanCell(tenMon, TextAlignment.LEFT, 12f).setBold().setPaddingLeft(10));
            mainTable.addCell(cleanCell(String.valueOf(item.getSoLuong()), TextAlignment.CENTER, 12f));
            mainTable.addCell(cleanCell(moTa, TextAlignment.LEFT, 11f));
        }
        document.add(mainTable);
    }

    private void drawModernBorder(PdfDocument pdf) {
        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        canvas.setStrokeColor(DEEP_RED).setLineWidth(1.2f).roundRectangle(15, 15, pdf.getDefaultPageSize().getWidth() - 30, pdf.getDefaultPageSize().getHeight() - 30, 8).stroke();
    }

    private Cell cleanCell(String txt, TextAlignment al, float fontSize) {
        return new Cell().add(new Paragraph(txt).setFontSize(fontSize)).setBorder(Border.NO_BORDER).setPaddingTop(3).setPaddingBottom(3).setBorderBottom(new SolidBorder(LINE_COLOR, 0.1f)).setTextAlignment(al);
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

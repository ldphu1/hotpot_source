package com.fourspicehotpot.backend.common.exporter;

import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangExportService {

    private final KhachHangRepository repo;

    /* =====================================================
       EXPORT ALL
    ===================================================== */
    public byte[] exportExcel() throws Exception {
        return buildExcel(repo.findAll());
    }

    /* =====================================================
       EXPORT SELECTED IDS
    ===================================================== */
    public byte[] exportExcelByIds(List<Long> ids) throws Exception {
        List<KhachHang> list = repo.findAllById(ids);
        return buildExcel(list);
    }

    /* =====================================================
       CORE BUILD EXCEL (DÙNG CHUNG)
    ===================================================== */
    private byte[] buildExcel(List<KhachHang> list) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("KhachHang");

        /* ===== HEADER ===== */
        String[] cols = {
                "STT",
                "Họ tên",
                "Giới tính",
                "Ngày sinh",
                "SĐT",
                "Email",
                "Địa chỉ mặc định",
                "Trạng thái"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            header.createCell(i).setCellValue(cols[i]);
        }

        /* ===== DATA ===== */
        int rowIndex = 1;


        for (KhachHang kh : list) {

            Row row = sheet.createRow(rowIndex);

            row.createCell(0).setCellValue(rowIndex);
            row.createCell(1).setCellValue(kh.getTenKhachHang());
            row.createCell(2).setCellValue(kh.getGioiTinh() ? "Nam" : "Nữ");

            row.createCell(3).setCellValue(
                    kh.getNgaySinh() != null
                            ? kh.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : ""
            );


            row.createCell(4).setCellValue(kh.getSoDienThoai());
            row.createCell(5).setCellValue(kh.getEmail());

            /* ===== ĐỊA CHỈ MẶC ĐỊNH ===== */
            String diaChi = "—";

            if (kh.getDiaChiList() != null && !kh.getDiaChiList().isEmpty()) {

                DiaChiKhachHang dc = kh.getDiaChiList()
                        .stream()
                        .filter(DiaChiKhachHang::getMacDinh)
                        .findFirst()
                        .orElse(kh.getDiaChiList().get(0));

                diaChi = dc.getDiaChiChiTiet()
                        + ", " + dc.getXaPhuong()
                        + ", " + dc.getTinhThanh();
            }

            row.createCell(6).setCellValue(diaChi);

            row.createCell(7).setCellValue(
                    kh.getTrangThai() == 1 ? "Hoạt động" : "Ngừng"
            );

            rowIndex++;
        }

        /* ===== AUTO SIZE ===== */
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }

        /* ===== WRITE FILE ===== */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}

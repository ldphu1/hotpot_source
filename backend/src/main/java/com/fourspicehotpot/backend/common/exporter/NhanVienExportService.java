package com.fourspicehotpot.backend.common.exporter;

import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienExportService {

    private final NhanVienRepository repository;

    public byte[] exportExcelByIds(List<Long> ids) throws Exception {

        List<NhanVien> list = repository.findAllById(ids);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("NhanVien");

        Row header = sheet.createRow(0);

        String[] cols = {
                "STT", "Tên", "Giới tính",
                "Ngày sinh", "SĐT", "Email",
                "Chức vụ", "Trạng thái", "Địa chỉ"
        };

        for (int i = 0; i < cols.length; i++) {
            header.createCell(i).setCellValue(cols[i]);
        }

        int rowIndex = 1;

        for (NhanVien nv : list) {

            Row row = sheet.createRow(rowIndex);

            row.createCell(0).setCellValue(rowIndex);
            row.createCell(1).setCellValue(nv.getTenNhanVien());
            row.createCell(2).setCellValue(nv.getGioiTinh() ? "Nam" : "Nữ");
            row.createCell(3).setCellValue(
                    nv.getNgaySinh() != null ? nv.getNgaySinh().toString() : ""
            );
            row.createCell(4).setCellValue(nv.getSoDienThoai());
            row.createCell(5).setCellValue(nv.getEmail());
            row.createCell(6).setCellValue(
                    nv.getChucVu() == 1 ? "Quản lý" : "Nhân viên"
            );
            row.createCell(7).setCellValue(
                    nv.getTrangThaiXoa() ? "Ngừng" : "Hoạt động"
            );

            row.createCell(8).setCellValue(
                    nv.getDiaChiChiTiet() + ", " +
                            nv.getXaPhuong() + ", " +
                            nv.getTinhThanh()
            );

            rowIndex++;
        }

        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}


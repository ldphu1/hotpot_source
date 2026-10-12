package com.fourspicehotpot.backend.common.importer;

import com.fourspicehotpot.backend.modules.nhanvien.service.NhanVienService;
import com.fourspicehotpot.backend.modules.nhanvien.dto.request.NhanVienRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienImportService {

    private final NhanVienService nhanVienService;
    private final DataFormatter formatter = new DataFormatter();

    /* ================= HELPER ================= */

    public List<String> importExcel(MultipartFile file) throws Exception {

        List<String> errors = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                NhanVienRequest dto = new NhanVienRequest();


                dto.setTenNhanVien(getString(row, 0));
                dto.setNgaySinh(getDate(row, 1));
                dto.setGioiTinh(getBoolean(row, 2));
                dto.setSoDienThoai(getString(row, 3));
                dto.setEmail(getString(row, 4));
                dto.setChucVu((int) getNumeric(row, 5));
                dto.setTinhThanh(getString(row, 6));
                dto.setXaPhuong(getString(row, 7));
                dto.setDiaChiChiTiet(getString(row, 8));

                nhanVienService.add(dto, null);

            } catch (Exception e) {
                errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
            }
        }

        return errors;
    }

    private String getString(Row r, int c) {
        Cell cell = r.getCell(c);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }


    private double getNumeric(Row r, int c) {
        Cell cell = r.getCell(c);
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC)
            return cell.getNumericCellValue();

        if (cell.getCellType() == CellType.STRING)
            return Double.parseDouble(cell.getStringCellValue());

        return 0;
    }


    private Boolean getBoolean(Row r, int c) {
        String val = getString(r, c).toLowerCase();
        return val.contains("nam");
    }

    private LocalDate getDate(Row r, int c) {
        Cell cell = r.getCell(c);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        throw new RuntimeException("Ngày sinh không đúng định dạng");
    }
}

package com.fourspicehotpot.backend.modules.nhanvien.controller;

import com.fourspicehotpot.backend.modules.nhanvien.service.NhanVienService;
import com.fourspicehotpot.backend.common.exporter.NhanVienExportService;
import com.fourspicehotpot.backend.common.importer.NhanVienImportService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.nhanvien.dto.request.NhanVienRequest;
import com.fourspicehotpot.backend.modules.nhanvien.dto.response.NhanVienResponse;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;
    @Autowired
    private NhanVienImportService importService;

    @Autowired
    private NhanVienExportService exportService;

    // ================== HIỂN THỊ ==================
    @RequirePermission("XEM_NHAN_VIEN")
    @GetMapping("/hien-thi")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(nhanVienService.findAll());
    }

    // ================== CHỨC VỤ ==================
    @RequirePermission("THEM_NHAN_VIEN")
    @GetMapping("/chuc-vu")
    public List<Map<String, Object>> getChucVu() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(Map.of("value", 0, "label", "Nhân viên"));
        list.add(Map.of("value", 1, "label", "Quản lý"));

        return list;
    }

    // ================== GIỚI TÍNH ==================
    @RequirePermission("THEM_NHAN_VIEN")
    @GetMapping("/gioi-tinh")
    public List<Map<String, Object>> getGioiTinh() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(Map.of("value", true, "label", "Nam"));
        list.add(Map.of("value", false, "label", "Nữ"));

        return list;
    }

    // ================== THÊM NHÂN VIÊN ==================
    @RequirePermission("THEM_NHAN_VIEN")
    @PostMapping(
            value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<NhanVien> add(
            @Valid @RequestPart("data") NhanVienRequest dto,
            BindingResult result,
            @RequestPart(value = "anh", required = false) MultipartFile anh
    ) throws IOException {

        if (result.hasErrors()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    result.getFieldError().getDefaultMessage()
            );
        }

        NhanVien nv = nhanVienService.add(dto, anh);
        return ResponseEntity.ok(nv);
    }

    // ================== MAP RESPONSE ==================
    private NhanVienResponse mapToResponse(NhanVien nv) {
        return NhanVienResponse.builder()
                .id(nv.getId())
                .tenNhanVien(nv.getTenNhanVien())
                .email(nv.getEmail())
                .soDienThoai(nv.getSoDienThoai())
                .anhNhanVien(nv.getAnhNhanVien())
                .chucVu(nv.getChucVu())
                .trangThaiXoa(nv.getTrangThaiXoa())
                .build();
    }

    @RequirePermission("XEM_NHAN_VIEN")
    @GetMapping("/detail/{id}")
    public ResponseEntity<NhanVienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(nhanVienService.getById(id));
    }

    // ================== CẬP NHẬT NHÂN VIÊN ==================
    @RequirePermission("SUA_NHAN_VIEN")
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateNhanVien(
            @PathVariable Long id,
            @Valid @RequestPart("data") NhanVienRequest dto,
            BindingResult result,
            @RequestPart(value = "anhNhanVien", required = false) MultipartFile anhNhanVien
    ) throws IOException {

        if (result.hasErrors()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    result.getFieldError().getDefaultMessage()
            );
        }

        NhanVien nv = nhanVienService.update(id, dto, anhNhanVien);
        return ResponseEntity.ok(mapToResponse(nv));
    }

    @RequirePermission("SUA_NHAN_VIEN")
    @PutMapping("/doi-trang-thai/{id}")
    public ResponseEntity<?> doiTrangThai(@PathVariable Long id) {
        nhanVienService.doiTrangThai(id);
        return ResponseEntity.ok().build();
    }

    @RequirePermission("XEM_NHAN_VIEN")
    @GetMapping("/search")
    public ResponseEntity<List<NhanVienResponse>> searchNhanVien(
            @RequestParam(required = false) String ten,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false) Boolean trangThai
    ) {
        return ResponseEntity.ok(
                nhanVienService.search(ten, contact, trangThai)
        );
    }

    //import excel
    @RequirePermission("THEM_NHAN_VIEN")
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body("File rỗng");

        String name = file.getOriginalFilename();
        if (name == null || !name.endsWith(".xlsx"))
            return ResponseEntity.badRequest().body("Chỉ chấp nhận file Excel .xlsx");

        try {
            List<String> errors = importService.importExcel(file);

            return ResponseEntity.ok(Map.of(
                    "success", errors.isEmpty(),
                    "errors", errors
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // file mẫu impor
    @RequirePermission("THEM_NHAN_VIEN")
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("NhanVien");

        String[] headers = {
                "Tên nhân viên", "Ngày sinh", "Giới tính",
                "SĐT", "Email", "Chức vụ", "Tỉnh", "Xã", "Địa chỉ"
        };

        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=nhan-vien-template.xlsx")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }

    //export excel
    @RequirePermission("XEM_NHAN_VIEN")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSelected(@RequestBody List<Long> ids) throws Exception {

        byte[] file = exportService.exportExcelByIds(ids);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=nhan-vien.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }


}

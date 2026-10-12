package com.fourspicehotpot.backend.modules.nhanvien.service.serviceImpl;

import com.fourspicehotpot.backend.modules.nhanvien.service.NhanVienService;
import com.fourspicehotpot.backend.integration.mail.utils.SendMail;
import com.fourspicehotpot.backend.modules.nhanvien.dto.request.NhanVienRequest;
import com.fourspicehotpot.backend.modules.nhanvien.dto.response.NhanVienResponse;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;


@Service

public class NhanVienServiceImpl implements NhanVienService {
    @Autowired
    NhanVienRepository nhanVienRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public List<NhanVienResponse> findAll() {
        return nhanVienRepo.findAll()
                .stream()
                .map(NhanVienResponse::fromEntity)
                .toList();
    }


    @Override
    public NhanVien add(NhanVienRequest dto, MultipartFile anh) throws IOException {

        // ===== CHECK TRÙNG =====


        if (nhanVienRepo.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email đã tồn tại");

        if (nhanVienRepo.existsBySoDienThoai(dto.getSoDienThoai()))
            throw new IllegalArgumentException("SĐT đã tồn tại");

        // ===== TẠO ENTITY =====
        NhanVien nv = new NhanVien();

        // ===== MAP FIELD =====

        nv.setTenNhanVien(dto.getTenNhanVien());
        nv.setNgaySinh(dto.getNgaySinh());
        nv.setGioiTinh(dto.getGioiTinh());
        nv.setSoDienThoai(dto.getSoDienThoai());
        nv.setEmail(dto.getEmail());
        nv.setChucVu(dto.getChucVu());
        nv.setTinhThanh(dto.getTinhThanh());
        nv.setXaPhuong(dto.getXaPhuong());
        nv.setDiaChiChiTiet(dto.getDiaChiChiTiet());

        // ===== AUTO ACCOUNT =====
        String username = "nv"
                + dto.getSoDienThoai().substring(dto.getSoDienThoai().length() - 4)
                + (System.currentTimeMillis() % 1000);

        String rawPassword = "123456"; // có thể random sau

        nv.setTaiKhoan(username);
        nv.setMatKhau(passwordEncoder.encode(rawPassword));

        // ===== AVATAR =====
        nv.setAnhNhanVien(saveAvatar(anh));

        // ===== DEFAULT =====
        nv.setTrangThai(1);
        nv.setTrangThaiXoa(false);
        nv.setNgayTao(LocalDateTime.now());

        // ===== SAVE DB =====
        NhanVien savedNv = nhanVienRepo.save(nv);

        // ===== SEND MAIL =====
        String mailContent = """
                Chào bạn,<br><br>
                
                FourSpiceHotpot rất vui mừng chào đón bạn chính thức trở thành thành viên của đại gia đình chúng tôi.<br><br>
                
                <b>Thông tin tài khoản làm việc</b><br>
                • Tên đăng nhập: <b>%s</b><br>
                • Mật khẩu: <b>%s</b><br><br>
                
                Vui lòng đăng nhập và đổi mật khẩu ngay trong lần truy cập đầu tiên để đảm bảo bảo mật thông tin.<br><br>
                
                Trân trọng,<br>
                Phòng Nhân sự – FourSpiceHotpot
                """.formatted(username, rawPassword);

        try {
            SendMail.sendMail(
                    savedNv.getEmail(),
                    "Thông tin tài khoản nhân viên – FourSpiceHotpot",
                    mailContent
            );
        } catch (Exception e) {
            // ❗ KHÔNG throw để tránh rollback tạo nhân viên
            e.printStackTrace();
        }

        return savedNv;
    }

    private String saveAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String fileName = System.currentTimeMillis()
                + "_" + file.getOriginalFilename();

        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);

        File output = uploadDir.resolve(fileName).toFile();

        Thumbnails.of(file.getInputStream())
                .size(300, 300)
                .outputQuality(0.8)
                .toFile(output);

        return fileName;
    }

    @Override
    public NhanVienResponse getById(Long id) {
        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        return NhanVienResponse.fromEntity(nv);
    }

    @Override
    public NhanVien update(Long id, NhanVienRequest dto, MultipartFile anhNhanVien) throws IOException {

        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (nhanVienRepo.existsByEmailAndIdNot(dto.getEmail(), id))
            throw new IllegalArgumentException("Email đã tồn tại");

        if (nhanVienRepo.existsBySoDienThoaiAndIdNot(dto.getSoDienThoai(), id))
            throw new IllegalArgumentException("SĐT đã tồn tại");

        // ===== update field =====

        nv.setTenNhanVien(dto.getTenNhanVien());
        nv.setNgaySinh(dto.getNgaySinh());
        nv.setGioiTinh(dto.getGioiTinh());
        nv.setSoDienThoai(dto.getSoDienThoai());
        nv.setEmail(dto.getEmail());
        nv.setChucVu(dto.getChucVu());
        nv.setTinhThanh(dto.getTinhThanh());
        nv.setXaPhuong(dto.getXaPhuong());
        nv.setDiaChiChiTiet(dto.getDiaChiChiTiet());


        // ===== nếu có ảnh mới thì thay =====
        if (anhNhanVien != null && !anhNhanVien.isEmpty()) {
            String fileName = saveAvatar(anhNhanVien);// hàm m dùng bên add
            nv.setAnhNhanVien(fileName);
        }

        return nhanVienRepo.save(nv);
    }

    public void doiTrangThai(Long id) {
        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        nv.setTrangThaiXoa(!nv.getTrangThaiXoa());
        nhanVienRepo.save(nv);
    }

    public List<NhanVienResponse> search(
            String ten,
            String contact,
            Boolean trangThai
    ) {
        return nhanVienRepo.search(ten, contact, trangThai)
                .stream()
                .map(NhanVienResponse::fromEntity)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return nhanVienRepo.existsByEmail(email);
    }


    @Override
    public boolean existsBySoDienThoai(String phone) {
        return nhanVienRepo.existsBySoDienThoai(phone);
    }


}

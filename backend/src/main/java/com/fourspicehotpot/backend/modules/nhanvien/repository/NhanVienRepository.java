package com.fourspicehotpot.backend.modules.nhanvien.repository;

import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {


    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsBySoDienThoaiAndIdNot(String phone, Long id);



    @Query("""
            SELECT nv FROM NhanVien nv
            WHERE
            (:ten IS NULL OR LOWER(nv.tenNhanVien) LIKE LOWER(CONCAT('%', :ten, '%')))
            AND (
                :contact IS NULL
                OR nv.email LIKE CONCAT('%', :contact, '%')
                OR nv.soDienThoai LIKE CONCAT('%', :contact, '%')
            )
            AND (
                :trangThai IS NULL
                OR (:trangThai = true AND nv.trangThaiXoa = false)
                OR (:trangThai = false AND nv.trangThaiXoa = true)
            )
            ORDER BY nv.id DESC
            """)
    List<NhanVien> search(
            @Param("ten") String ten,
            @Param("contact") String contact,
            @Param("trangThai") Boolean trangThai
    );

    // LÃNH MINH HIẾU
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan = :tk AND n.matKhau = :mk")
    NhanVien findByTaiKhoanAndMatKhau(@Param("tk") String tk, @Param("mk") String mk);


    @Query("""
            SELECT p.tenPermission
            FROM NhanVien nv
            JOIN RolePermission rp ON nv.chucVu = rp.roleId
            JOIN Permission p ON rp.permissionId = p.id
            WHERE nv.id = :id
            """)
    List<String> getPermissions(@Param("id") Long id);

    NhanVien findByTaiKhoan(String tk);

    Optional<NhanVien> findNhanVienByTaiKhoan(String taiKhoan);


}

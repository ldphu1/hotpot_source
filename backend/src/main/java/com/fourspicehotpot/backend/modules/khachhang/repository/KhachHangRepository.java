package com.fourspicehotpot.backend.modules.khachhang.repository;

import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    List<KhachHang> findAllKhachHangById(Integer id);

    @Query("""
                select distinct kh
                from KhachHang kh
                left join fetch kh.diaChiList
            """)
    List<KhachHang> findAllWithDiaChi();

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);


    // LANH MINH HIEU
    KhachHang findByEmail(String email);


    List<KhachHang> findKhachHangByEmail(String email);
    // Optional<KhachHang> findKhachHangByEmail(String email);

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    List<KhachHang> findByTenKhachHangContainingIgnoreCaseOrSoDienThoaiContaining(
            String ten,
            String sdt
    );

    List<KhachHang> findByTenKhachHangContainingIgnoreCase(String keyword);


    // check khách hàng có đẹp trai bằng long ko
    @Query("""
                select kh
                from KhachHang kh
                where kh.maKhachHang = :maKhachHang
            """)
    Optional<KhachHang> findByMaKhachHang(@Param("maKhachHang") String maKhachHang);
}

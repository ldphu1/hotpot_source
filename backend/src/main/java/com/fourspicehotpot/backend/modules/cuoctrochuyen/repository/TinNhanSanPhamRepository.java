package com.fourspicehotpot.backend.modules.cuoctrochuyen.repository;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhanSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanSanPhamRepository extends JpaRepository<TinNhanSanPham, Long> {
    List<TinNhanSanPham> findByTinNhanIdIn(List<Long> tinNhanIds);
}
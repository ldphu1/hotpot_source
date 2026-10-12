package com.fourspicehotpot.backend.modules.thamsohethong.repository;

import com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThamSoHeThongRepository extends JpaRepository<ThamSoHeThong, Long> {

    Optional<ThamSoHeThong> findByMaThamSoAndTrangThaiTrue(String maThamSo);

    boolean existsByMaThamSo(String maThamSo);

    boolean existsByMaThamSoAndIdNot(String maThamSo, Long id);
}

package com.fourspicehotpot.backend.modules.setlau.repository;

import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetThanhPhanRepository extends JpaRepository<SetThanhPhan, Long> {
    List<SetThanhPhan> findBySetIdAndTrangThaiXoaFalse(Long idSet);
}

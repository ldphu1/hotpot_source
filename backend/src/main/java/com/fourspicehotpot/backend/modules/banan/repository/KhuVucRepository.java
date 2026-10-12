package com.fourspicehotpot.backend.modules.banan.repository;

import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhuVucRepository extends JpaRepository<KhuVuc, Integer> {
}

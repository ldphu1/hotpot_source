package com.fourspicehotpot.backend.modules.setlau.repository;

import com.fourspicehotpot.backend.modules.setlau.entity.NuocLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NuocLauRepository extends JpaRepository<NuocLau, Long>, JpaSpecificationExecutor<NuocLau> {

    @Query("""
            select nl from NuocLau nl order by nl.id desc 
            """)
    List<NuocLau> getAllNuocLau();
}

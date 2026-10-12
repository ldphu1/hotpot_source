package com.fourspicehotpot.backend.modules.banan.repository;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.checkin.dto.response.BanAnTrongResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanAnRepository extends JpaRepository<BanAn, Integer>, JpaSpecificationExecutor<BanAn> {

    long countByKhuVucIdAndTrangThaiNot(Integer khuVucId, Integer trangThai);

    @Query("""
                select distinct b
                from BanAn b
                left join fetch b.khuVuc kv
                order by b.id asc
            """)
    List<BanAn> findAllWithKhuVuc();

    @Query("""
                SELECT b FROM BanAn b
                WHERE b.tenBanAn LIKE %:keyword%
                OR b.maBanAn LIKE %:keyword%
            """)
    List<BanAn> searchBanAn(@Param("keyword") String keyword);

    @Query("SELECT b FROM BanAn b WHERE b.trangThai IN (1,2)")
    List<BanAn> getBanTrongVaDaDat();

    List<BanAn> findByTrangThaiIn(Collection<Integer> trangThais);


    // check in
    @Query("""
                select b
                from BanAn b
                where b.id = :idBanAn
            """)
    Optional<BanAn> findBanAnById(@Param("idBanAn") Integer idBanAn);

    @Modifying
    @Query("""
                update BanAn b
                set b.trangThai = :trangThai
                where b.id = :idBan
            """)
    void updateTrangThai(@Param("idBan") Long idBan,
                         @Param("trangThai") Integer trangThai);

    @Query("""
            select new com.fourspicehotpot.backend.modules.checkin.dto.response.BanAnTrongResponse(
                    b.id,
                    b.maBanAn,
                    b.tenBanAn,
                    b.sucChuaToiDa,
                    b.trangThai,
                    kv.id,
                    kv.tenKhuVuc
                )
                from BanAn b
                left join b.khuVuc kv
                where b.trangThai = 1
                order by kv.id, b.tenBanAn
            """)
    List<BanAnTrongResponse> findAllBanTrong();


    /// ///////// BỔ SUNG - CẤM SỬA //////////////
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM BanAn b
            WHERE b.id IN :ids
            """)
    List<BanAn> findAllByIdForUpdate(@Param("ids") List<Integer> ids);

    List<BanAn> findByTrangThai(Integer trangThai);
}

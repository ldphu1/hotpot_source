package com.fourspicehotpot.backend.modules.banan.service;

import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;

import java.util.List;

public interface KhuVucService {

    List<KhuVuc> findAll();

    KhuVuc findById(Integer id);

    KhuVuc save(KhuVuc khuVuc);

    void deleteKhuVuc(Integer id);
}

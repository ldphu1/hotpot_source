package com.fourspicehotpot.backend.modules.banan.service.serviceImpl;

import com.fourspicehotpot.backend.modules.banan.service.KhuVucService;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.repository.KhuVucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhuVucServiceImpl implements KhuVucService {

    @Autowired
    private KhuVucRepository khuVucRepository;


    @Override
    public List<KhuVuc> findAll() {
        return khuVucRepository.findAll();
    }

    @Override
    public KhuVuc findById(Integer id) {
        return khuVucRepository.findById(id).orElse(null);
    }

    @Override
    public KhuVuc save(KhuVuc khuVuc) {
        return khuVucRepository.save(khuVuc);
    }

    @Override
    public void deleteKhuVuc(Integer id) {
        KhuVuc khuVuc = khuVucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));
        khuVuc.setTrangThai(0);
        khuVucRepository.save(khuVuc);
    }
}

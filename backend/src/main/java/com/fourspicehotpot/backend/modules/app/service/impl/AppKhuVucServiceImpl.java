package com.fourspicehotpot.backend.modules.app.service.impl;

import com.fourspicehotpot.backend.modules.app.dto.response.KhuVucAppResponse;
import com.fourspicehotpot.backend.modules.app.service.AppKhuVucService;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.repository.KhuVucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppKhuVucServiceImpl implements AppKhuVucService {

    @Autowired
    private KhuVucRepository khuVucRepository;

    @Override
    public List<KhuVucAppResponse> getDanhSachKhuVuc() {
        List<KhuVuc> khuVucList = khuVucRepository.findAll();
        return khuVucList.stream()
                .map(kv -> KhuVucAppResponse.builder()
                        .id(kv.getId())
                        .tenKhuVuc(kv.getTenKhuVuc())
                        .build())
                .collect(Collectors.toList());
    }
}

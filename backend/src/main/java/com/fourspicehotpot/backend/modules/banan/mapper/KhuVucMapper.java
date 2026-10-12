package com.fourspicehotpot.backend.modules.banan.mapper;

import com.fourspicehotpot.backend.modules.banan.dto.response.KhuVucResponse;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import org.springframework.stereotype.Component;

@Component
public class KhuVucMapper {

    public KhuVucResponse mapToResponse(KhuVuc khuVuc) {

        return KhuVucResponse.builder()
                .id(khuVuc.getId())
                .maKhuVuc(khuVuc.getMaKhuVuc())
                .tenKhuVuc(khuVuc.getTenKhuVuc())
                .moTa(khuVuc.getMoTa())
                .viTri(khuVuc.getViTri())
                .trangThai(khuVuc.getTrangThai())
                .build();
    }
}

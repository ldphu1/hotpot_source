package com.fourspicehotpot.backend.modules.setlau.service;

import com.fourspicehotpot.backend.modules.setlau.dto.request.NuocLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.NuocLauResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NuocLauService {
    List<NuocLauResponse> getAllNuocLau();

    NuocLauResponse getNuocLauById(Long id);

    void addNuocLau(NuocLauRequest request, MultipartFile file);

    NuocLauResponse updateNuocLau(Long id, NuocLauRequest request, MultipartFile file);

    List<NuocLauResponse> searchNuocLau(NuocLauRequest req);

    void chuyenTrangThai(Integer id);
}

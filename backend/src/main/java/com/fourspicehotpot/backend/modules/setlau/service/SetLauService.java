package com.fourspicehotpot.backend.modules.setlau.service;

import com.fourspicehotpot.backend.modules.setlau.dto.request.SetLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SetLauService {

    void addSetLau(SetLauRequest dto, MultipartFile file);

    SanPhamResponse findSetLauById(Integer id);

    void updateSetLau(Integer id, SetLauRequest dto, MultipartFile file);


}

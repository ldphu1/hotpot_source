package com.fourspicehotpot.backend.modules.setlau.service;

import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;

import java.util.List;

public interface SetThanhPhanService {

    List<SetThanhPhan> findAllSetThanhPhan();

    SetThanhPhan findById(Long id);
}

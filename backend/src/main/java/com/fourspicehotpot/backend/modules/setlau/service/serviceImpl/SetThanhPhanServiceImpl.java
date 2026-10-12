package com.fourspicehotpot.backend.modules.setlau.service.serviceImpl;

import com.fourspicehotpot.backend.modules.setlau.service.SetThanhPhanService;
import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;
import com.fourspicehotpot.backend.modules.setlau.repository.SetThanhPhanRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SetThanhPhanServiceImpl implements SetThanhPhanService {
    @Autowired
    private SetThanhPhanRepository setThanhPhanRepository;

    @Override
    public List<SetThanhPhan> findAllSetThanhPhan() {
        return setThanhPhanRepository.findAll();
    }

    @Override
    public SetThanhPhan findById(Long id) {
        return setThanhPhanRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy set thành phần"));
    }


}

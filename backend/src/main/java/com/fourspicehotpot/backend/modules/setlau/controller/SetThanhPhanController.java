package com.fourspicehotpot.backend.modules.setlau.controller;


import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;
import com.fourspicehotpot.backend.modules.setlau.service.SetThanhPhanService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/set-thanh-phan")
@CrossOrigin(originPatterns = "http://localhost:*")
public class SetThanhPhanController {

    @Autowired
    private SetThanhPhanService setThanhPhanService;

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/hien-thi")
    public List<SetThanhPhan> findAll() {

        return setThanhPhanService.findAllSetThanhPhan();
    }

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/detail/{id}")
    public SetThanhPhan findById(@PathVariable Long id) {

        return setThanhPhanService.findById(id);
    }
}

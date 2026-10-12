package com.fourspicehotpot.backend.modules.cuoctrochuyen.controller;

import com.fourspicehotpot.backend.common.security.jwt.JwtUtil;
import com.fourspicehotpot.backend.infrastructure.constant.MappingConstant;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.TinNhanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstant.API_TIN_NHAN)
public class TinNhanController {
    private final TinNhanService tinNhanService;
    private final JwtUtil jwtUtil;


}

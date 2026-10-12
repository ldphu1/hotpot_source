package com.fourspicehotpot.backend.modules.auth.service;

import com.fourspicehotpot.backend.modules.auth.dto.request.CustomerLoginRequest;

import java.util.Map;

public interface CustomerAuthService {
    Map<String, Object> login(CustomerLoginRequest request);


}

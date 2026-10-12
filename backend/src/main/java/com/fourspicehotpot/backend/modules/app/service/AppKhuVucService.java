package com.fourspicehotpot.backend.modules.app.service;

import com.fourspicehotpot.backend.modules.app.dto.response.KhuVucAppResponse;

import java.util.List;

public interface AppKhuVucService {

    List<KhuVucAppResponse> getDanhSachKhuVuc();
}

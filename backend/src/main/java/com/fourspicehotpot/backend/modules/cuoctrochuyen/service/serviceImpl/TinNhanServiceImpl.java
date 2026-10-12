package com.fourspicehotpot.backend.modules.cuoctrochuyen.service.serviceImpl;

import com.fourspicehotpot.backend.common.dto.ApiResponseCommon;
import com.fourspicehotpot.backend.infrastructure.constant.CursorPageConstants;
import com.fourspicehotpot.backend.infrastructure.redis.RedisKeyConstants;
import com.fourspicehotpot.backend.infrastructure.redis.RedisService;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.chatbot.dto.response.ChatResponse;
import com.fourspicehotpot.backend.modules.chatbot.integration.ai.OpenAIClient;
import com.fourspicehotpot.backend.modules.chatbot.service.ChatService;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.GuiTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.TinNhanProjection;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.TinNhanResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhan;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhanSanPham;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.LoaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.TrangThaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.mapper.mapstruct.CuocTroChuyenMapper;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.mapper.mapstruct.TinNhanMapper;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.page.CursorPageResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.repository.CuocTroChuyenRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.repository.TinNhanRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.repository.TinNhanSanPhamRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.TinNhanService;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.mapper.SanPhamMapper;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TinNhanServiceImpl implements TinNhanService {

    private final TinNhanRepository tinNhanRepository;
    private final TinNhanMapper tinNhanMapper;
    private final CuocTroChuyenRepository cuocTroChuyenRepository;
    private final RedisService redisService;
    private final CuocTroChuyenMapper cuocTroChuyenMapper;
    private final OpenAIClient openAIClient;
    private final ChatService chatService;
    private final WebSocketService webSocketService;
    private final SanPhamRepository sanPhamRepository;
    private final TinNhanSanPhamRepository tinNhanSanPhamRepository;
    private final SanPhamMapper sanPhamMapper;


    @Override
    public ApiResponseCommon<CursorPageResponse<TinNhanResponse>> getTinNhan(Long idCuocTroChuyen, LocalDateTime cursor) {
        int limit = CursorPageConstants.DEFAULT_PAGE_SIZE;

        List<TinNhanProjection> list = new ArrayList<>(tinNhanRepository.getTinNhanTheoCuocTroChuyen(
                idCuocTroChuyen, cursor, limit + 1
        ));

        boolean hasNext = list.size() > limit;
        if (hasNext) list = list.subList(0, limit);

        LocalDateTime nextCursor = hasNext
                ? list.get(list.size() - 1).getThoiGian()
                : null;

        Collections.reverse(list);

        List<Long> tinNhanIds = list.stream()
                .map(TinNhanProjection::getIdTinNhan)
                .toList();

        Map<Long, List<SanPhamResponse>> productsMap = tinNhanSanPhamRepository
                .findByTinNhanIdIn(tinNhanIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tnsp -> tnsp.getTinNhan().getId(),
                        Collectors.mapping(tnsp -> sanPhamMapper.mapToResponse(tnsp.getSanPham()), Collectors.toList())
                ));

        List<TinNhanResponse> result = list.stream().map(tin -> TinNhanResponse.builder()
                .idTinNhan(tin.getIdTinNhan())
                .noiDung(tin.getNoiDung())
                .loaiTinNhan(tin.getLoaiTinNhan())
                .daDoc(tin.getDaDoc())
                .thoiGian(tin.getThoiGian())
                .loaiNguoiGui(tin.getLoaiNguoiGui())
                .tenKhachHang(tin.getTenKhachHang())
                .tenNhanVien(tin.getTenNhanVien())
                .duongDanFile(tin.getDuongDanFile())
                .products(productsMap.getOrDefault(tin.getIdTinNhan(), new ArrayList<>()))
                .build()
        ).toList();

        return ApiResponseCommon.success(
                CursorPageResponse.<TinNhanResponse>builder()
                        .data(result)
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build(),
                HttpStatus.OK,
                "Thành công"
        );
    }

    @Override
    @Transactional
    public ApiResponseCommon<ChatRealtimeResponse> guiTinNhan(GuiTinNhanRequest request, Long idKhachHang) {

        CuocTroChuyen cuocTroChuyen = timHoacTaoCuocTroChuyen(request, idKhachHang);
        TinNhan tinNhanKhach = tinNhanRepository.save(
                tinNhanMapper.toEntityKhach(cuocTroChuyen, request, idKhachHang)
        );
        webSocketService.sendChatToTopic("/topic/chat", tinNhanMapper.toResponse(tinNhanKhach));

        // Chỉ AI trả lời khi trạng thái là BOT
        if (cuocTroChuyen.getLoai().equals(LoaiCuocTroChuyen.AI.name())) {
            ChatResponse aiResponse = chatService.askAI(request.getNoiDung());

            TinNhan tinNhanAI = tinNhanRepository.save(
                    tinNhanMapper.toEntityAI(cuocTroChuyen, aiResponse.getAnswer())
            );

            if (aiResponse.getProducts() != null && !aiResponse.getProducts().isEmpty()) {
                List<TinNhanSanPham> tinNhanSanPhams = aiResponse.getProducts().stream()
                        .map(sp -> TinNhanSanPham.builder()
                                .tinNhan(tinNhanAI)
                                .sanPham(sanPhamRepository.getReferenceById(sp.getId()))
                                .build())
                        .toList();
                tinNhanSanPhamRepository.saveAll(tinNhanSanPhams);
            }

            ChatRealtimeResponse guiTinNhanResponse = tinNhanMapper.toResponse(tinNhanAI);
            guiTinNhanResponse.setProducts(aiResponse.getProducts());
            webSocketService.sendChatToTopic("/topic/chat", guiTinNhanResponse);

            return ApiResponseCommon.success(guiTinNhanResponse, HttpStatus.OK, "Thành công");
        }

        // Trạng thái CHO_NHAN_VIEN hoặc DANG_XU_LY → chỉ gửi tin nhắn khách, không AI
        return ApiResponseCommon.success(
                tinNhanMapper.toResponse(tinNhanKhach),
                HttpStatus.OK,
                "Thành công"
        );
    }

    @Override
    public CuocTroChuyen timHoacTaoCuocTroChuyen(GuiTinNhanRequest request, Long idKhachHang) {
        if (idKhachHang != null) {
            return cuocTroChuyenRepository
                    .findByKhachHang_IdAndTrangThai(idKhachHang, TrangThaiCuocTroChuyen.DANG_MO)
                    .orElseGet(() -> cuocTroChuyenRepository.save(
                            cuocTroChuyenMapper.toEntity(idKhachHang, null)
                    ));
        } else {
            String sessionId = request.getSessionId();
            String idCuocTroChuyenStr = redisService.get(RedisKeyConstants.CHAT_SESSION + sessionId);

            if (idCuocTroChuyenStr != null) {
                return cuocTroChuyenRepository
                        .findById(Long.parseLong(idCuocTroChuyenStr))
                        .orElseGet(() -> taoCuocTroChuyenVangLai(sessionId));
            }
            return taoCuocTroChuyenVangLai(sessionId);
        }
    }

    @Override
    public CuocTroChuyen taoCuocTroChuyenVangLai(String sessionId) {
        CuocTroChuyen moi = cuocTroChuyenRepository.save(
                cuocTroChuyenMapper.toEntity(null, "Khách vãng lai")
        );
        redisService.set(RedisKeyConstants.CHAT_SESSION + sessionId, moi.getId().toString(), 7);
        return moi;
    }
}

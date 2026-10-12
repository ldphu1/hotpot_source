package com.fourspicehotpot.backend.modules.cuoctrochuyen.mapper.mapstruct;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.GuiTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhan;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.LoaiTinNhan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TinNhanMapper {

    default TinNhan toEntityKhach(CuocTroChuyen cuocTroChuyen, GuiTinNhanRequest request, Long idKhachHang) {
        return TinNhan.builder()
                .cuocTroChuyen(cuocTroChuyen)
                .idNguoiGui(idKhachHang)
                .noiDung(request.getNoiDung())
                .loaiNguoiGui("KHACH_HANG")
                .loaiTinNhan(request.getLoaiTinNhan())
                .build();
    }

    default TinNhan toEntityAI(CuocTroChuyen cuocTroChuyen, String noiDung) {
        return TinNhan.builder()
                .cuocTroChuyen(cuocTroChuyen)
                .idNguoiGui(null)
                .loaiNguoiGui("AI")
                .noiDung(noiDung)
                .loaiTinNhan(LoaiTinNhan.TEXT.name())
                .daDoc(true)
                .build();
    }

    default ChatRealtimeResponse toResponse(TinNhan tinNhan) {
        return ChatRealtimeResponse.builder()
                .idCuocTroChuyen(tinNhan.getCuocTroChuyen().getId())
                .idTinNhan(tinNhan.getId())
                .noiDung(tinNhan.getNoiDung())
                .loaiNguoiGui(tinNhan.getLoaiNguoiGui())
                .thoiGian(tinNhan.getThoiGian())
                .build();
    }
}

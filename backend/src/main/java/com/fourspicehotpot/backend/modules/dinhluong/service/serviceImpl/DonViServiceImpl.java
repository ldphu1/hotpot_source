package com.fourspicehotpot.backend.modules.dinhluong.service.serviceImpl;

import com.fourspicehotpot.backend.modules.dinhluong.dto.request.DonViRequest;
import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse;
import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DonViResponse;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DanhMucDinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DonVi;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DanhMucDinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DonViRepository;
import com.fourspicehotpot.backend.modules.dinhluong.service.DonViService;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import com.fourspicehotpot.backend.modules.thucdon.repository.DanhMucRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonViServiceImpl implements DonViService {

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private DinhLuongRepository dinhLuongRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private DanhMucDinhLuongRepository danhMucDinhLuongRepository;

    @Override
    public List<DonViResponse> getAllDonVi() {

        List<DonVi> lst = donViRepository.getAllDonViWithDinhLuong();

        return lst.stream().map(dv -> {

            List<DinhLuongResponse> dinhLuongResponses =
                    dv.getDinhLuongs().stream()
                            .filter(dl -> !dl.getTrangThaiXoa())
                            .map(dl -> DinhLuongResponse.builder()
                                    .id(dl.getId())
                                    .maDinhLuong(dl.getMaDinhLuong())
                                    .giaTri(dl.getGiaTri())
                                    .build())
                            .toList();

            // lấy danh mục
            List<String> danhMucs = dv.getDinhLuongs().stream()
                    .flatMap(dl -> dl.getDanhMucDinhLuongs().stream())
                    .map(dmdl -> dmdl.getDanhMuc().getTenDanhMuc())
                    .distinct()
                    .toList();

            return DonViResponse.builder()
                    .id(dv.getId())
                    .maDonVi(dv.getMaDonVi())
                    .tenDonVi(dv.getTenDonVi())
                    .soLuongDinhLuong(dinhLuongResponses.size())
                    .dinhLuongS(dinhLuongResponses)
                    .danhMucs(danhMucs)
                    .trangThaiXoa(dv.getTrangThaiXoa())
                    .build();

        }).toList();
    }

    @Override
    public List<DonViResponse> searchDonVi(String keyword, Boolean trangThaiXoa) {

        List<DonVi> lst = donViRepository.searchDonVi(keyword, trangThaiXoa);

        return lst.stream().map(dv -> {

            List<DinhLuongResponse> dinhLuongResponses =
                    dv.getDinhLuongs().stream()
                            .filter(dl -> !dl.getTrangThaiXoa())
                            .map(dl -> DinhLuongResponse.builder()
                                    .id(dl.getId())
                                    .maDinhLuong(dl.getMaDinhLuong())
                                    .giaTri(dl.getGiaTri())
                                    .build())
                            .toList();

            List<String> danhMucs = dv.getDinhLuongs().stream()
                    .flatMap(dl -> dl.getDanhMucDinhLuongs().stream())
                    .map(dmdl -> dmdl.getDanhMuc().getTenDanhMuc())
                    .distinct()
                    .toList();

            return DonViResponse.builder()
                    .id(dv.getId())
                    .maDonVi(dv.getMaDonVi())
                    .tenDonVi(dv.getTenDonVi())
                    .soLuongDinhLuong(dinhLuongResponses.size())
                    .dinhLuongS(dinhLuongResponses)
                    .danhMucs(danhMucs)
                    .trangThaiXoa(dv.getTrangThaiXoa())
                    .build();

        }).toList();
    }

    @Override
    public void addDonVi(DonViRequest request) {
        // 1 tạo đơn vị
        DonVi donVi = DonVi.builder()
                .tenDonVi(request.getTenDonVi())
                .trangThaiXoa(false)
                .build();
        DonVi donViSaved = donViRepository.save(donVi);
        // 2 tạo định lượng
        List<DinhLuong> dinhLuongs = request.getDinhLuongs()
                .stream()
                .map(giaTri -> {
                    DinhLuong dl = DinhLuong.builder()
                            .giaTri(giaTri)
                            .trangThaiXoa(false)
                            .donVi(donViSaved)
                            .build();
                    DinhLuong saved = dinhLuongRepository.save(dl);
                    return saved;
                }).toList();
        // 3 map danh mục
        List<DanhMuc> danhMucs = danhMucRepository.findAllById(request.getIdDanhMucs());

        List<DanhMucDinhLuong> mappings = new ArrayList<>();
        for (DanhMuc dm : danhMucs) {
            for (DinhLuong dl : dinhLuongs) {
                DanhMucDinhLuong map = DanhMucDinhLuong.builder()
                        .danhMuc(dm)
                        .dinhLuong(dl)
                        .build();

                mappings.add(map);
            }
        }
        danhMucDinhLuongRepository.saveAll(mappings);
    }

    @Override
    public void chuyenTrangThai(Long id) {

        DonVi donVi = donViRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));

        donVi.setTrangThaiXoa(!Boolean.TRUE.equals(donVi.getTrangThaiXoa()));

        donViRepository.save(donVi);
    }

    @Override
    public DonViResponse findById(Long id) {
        DonVi dv = donViRepository.getDetailDonVi(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));

        List<DinhLuongResponse> dinhLuongResponses =
                dv.getDinhLuongs().stream()
                        .filter(dl -> !dl.getTrangThaiXoa())
                        .map(dl -> DinhLuongResponse.builder()
                                .id(dl.getId())
                                .maDinhLuong(dl.getMaDinhLuong())
                                .giaTri(dl.getGiaTri())
                                .build())
                        .toList();

        List<String> danhMucs = dv.getDinhLuongs().stream()
                .flatMap(dl -> dl.getDanhMucDinhLuongs().stream())
                .map(dmdl -> dmdl.getDanhMuc().getTenDanhMuc())
                .distinct()
                .toList();

        return DonViResponse.builder()
                .id(dv.getId())
                .maDonVi(dv.getMaDonVi())
                .tenDonVi(dv.getTenDonVi())
                .soLuongDinhLuong(dinhLuongResponses.size())
                .dinhLuongS(dinhLuongResponses)
                .danhMucs(danhMucs)
                .trangThaiXoa(dv.getTrangThaiXoa())
                .build();
    }

    @Override
    @Transactional
    public void updateDonViTongHop(Long id, DonViRequest request) {

        DonVi donVi = donViRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn vị không tồn tại"));

        donVi.setTenDonVi(request.getTenDonVi());
        donViRepository.save(donVi);
        List<DinhLuong> oldDinhLuongs = dinhLuongRepository.findByDonViId(id);
        danhMucDinhLuongRepository.deleteByDinhLuongIn(oldDinhLuongs);
        dinhLuongRepository.deleteAll(oldDinhLuongs);

        List<DinhLuong> dinhLuongs = request.getDinhLuongs()
                .stream()
                .map(giaTri -> {
                    DinhLuong dl = DinhLuong.builder()
                            .giaTri(giaTri)
                            .trangThaiXoa(false)
                            .donVi(donVi)
                            .build();
                    return dinhLuongRepository.save(dl);
                }).toList();

        List<DanhMuc> danhMucs = danhMucRepository.findAllById(request.getIdDanhMucs());

        List<DanhMucDinhLuong> mappings = new ArrayList<>();
        for (DanhMuc dm : danhMucs) {
            for (DinhLuong dl : dinhLuongs) {

                DanhMucDinhLuong map = DanhMucDinhLuong.builder()
                        .danhMuc(dm)
                        .dinhLuong(dl)
                        .build();

                mappings.add(map);
            }
        }
        danhMucDinhLuongRepository.saveAll(mappings);
    }

}

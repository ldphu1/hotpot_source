package com.fourspicehotpot.backend.modules.thucdon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.dinhluong.entity.DanhMucDinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DanhMucDinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DonViRepository;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.DanhMucRequest;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import com.fourspicehotpot.backend.modules.thucdon.repository.DanhMucRepository;
import com.fourspicehotpot.backend.modules.thucdon.service.DanhMucService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DanhMucServiceImpl implements DanhMucService {
    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private DinhLuongRepository dinhLuongRepository;

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private DanhMucDinhLuongRepository danhMucDinhLuongRepository;

    @Override
    public List<DanhMucResponse> findAllDanhMucHoatDong() {
        return danhMucRepository.findAllDanhMucHoatDong()
                .stream()
                .map(danhMuc -> DanhMucResponse.builder()
                        .id(danhMuc.getId())
                        .maDanhMuc(danhMuc.getMaDanhMuc())
                        .tenDanhMuc(danhMuc.getTenDanhMuc())
                        .trangThaiXoa(danhMuc.getTrangThaiXoa())
                        .soLuongMon(danhMuc.getSoLuongMon())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<DanhMucResponse> findAllDanhMucMonLe() {
        return danhMucRepository.findDanhMucMonLe()
                .stream()
                .map(danhMuc -> DanhMucResponse.builder()
                        .id(danhMuc.getId())
                        .maDanhMuc(danhMuc.getMaDanhMuc())
                        .tenDanhMuc(danhMuc.getTenDanhMuc())
                        .trangThaiXoa(danhMuc.getTrangThaiXoa())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public void addDanhMuc(DanhMucRequest request) {
        if (danhMucRepository.existsByTenDanhMuc(request.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }
        DanhMuc danhMuc = DanhMuc.builder()
                .tenDanhMuc(request.getTenDanhMuc())
                .trangThaiXoa(false)
                .build();

        danhMucRepository.save(danhMuc);
        List<DinhLuong> dinhLuongs =
                dinhLuongRepository.findByDonViIds(request.getIdDonVi());

        for (DinhLuong dl : dinhLuongs) {

            DanhMucDinhLuong map = DanhMucDinhLuong.builder()
                    .danhMuc(danhMuc)
                    .dinhLuong(dl)
                    .build();
            danhMucDinhLuongRepository.save(map);

        }


    }


    @Override
    public DanhMuc findDanhMucById(Integer id) {
        return danhMucRepository.
                findById(id).orElseThrow(()
                        -> new EntityNotFoundException("Khong tim thay danh muc"));
    }

    @Override
    public DanhMucResponse getDanhMucById(Integer id) {
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        List<Integer> donViIds =
                danhMucDinhLuongRepository.getDonViIdsByDanhMuc(id);

        return DanhMucResponse.builder()
                .id(danhMuc.getId())
                .tenDanhMuc(danhMuc.getTenDanhMuc())
                .idDonVi(donViIds)
                .build();
    }

    @Override
    @Transactional
    public void updateDanhMuc(DanhMucRequest request) {
        DanhMuc danhMuc = danhMucRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        if (danhMucRepository.existsByTenDanhMucAndIdNot(
                request.getTenDanhMuc(), request.getId())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }
        danhMuc.setTenDanhMuc(request.getTenDanhMuc());
        danhMucRepository.save(danhMuc);
        danhMucDinhLuongRepository.deleteByDanhMucId(danhMuc.getId());

        if (request.getIdDonVi() == null || request.getIdDonVi().isEmpty()) {
            return;
        }
        List<DinhLuong> dinhLuongs =
                dinhLuongRepository.findByDonViIds(request.getIdDonVi());
        for (DinhLuong dl : dinhLuongs) {
            DanhMucDinhLuong map = new DanhMucDinhLuong();
            map.setDanhMuc(danhMuc);
            map.setDinhLuong(dl);
            danhMucDinhLuongRepository.save(map);
        }
    }

    @Override
    public void deleteDanhMuc(Integer id) {
        DanhMuc entity = danhMucRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong tim thay danh muc"));
        entity.setTrangThaiXoa(true);
        danhMucRepository.save(entity);
    }

    @Override
    public void restoreDanhMuc(Integer id) {
        DanhMuc entity = danhMucRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong tim thay danh muc"));
        entity.setTrangThaiXoa(false);
        danhMucRepository.save(entity);
    }

    @Override
    public List<DanhMucResponse> filterDanhMuc(String keyword, Integer idDanhMuc, Boolean trangThaiXoa) {
        return danhMucRepository.searchDanhMuc(keyword, trangThaiXoa, idDanhMuc);
    }
}

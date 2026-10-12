package com.fourspicehotpot.backend.modules.setlau.service.serviceImpl;

import com.fourspicehotpot.backend.modules.setlau.service.NuocLauService;
import com.fourspicehotpot.backend.modules.setlau.dto.request.NuocLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.NuocLauResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.NuocLau;
import com.fourspicehotpot.backend.modules.setlau.repository.NuocLauRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NuocLauServiceImpl implements NuocLauService {

    @Autowired
    private NuocLauRepository nuocLauRepository;


    @Override
    public List<NuocLauResponse> getAllNuocLau() {
        List<NuocLau> lstNuocLau = nuocLauRepository.getAllNuocLau();

        return lstNuocLau.stream().map(nl -> {
            NuocLauResponse res = new NuocLauResponse();
            res.setId(nl.getId());
            res.setMaNuocLau(nl.getMaNuocLau());
            res.setTenNuocLau(nl.getTenNuocLau());
            res.setGiaPhuThu(nl.getGiaPhuThu());
            res.setMoTa(nl.getMoTa());
            res.setTrangThaiXoa(nl.getTrangThaiXoa());
            return res;
        }).toList();
    }

    @Override
    public NuocLauResponse getNuocLauById(Long id) {
        NuocLau nuocLau = nuocLauRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nước lẩu"));

        return NuocLauResponse.builder()
                .id(nuocLau.getId())
                .maNuocLau(nuocLau.getMaNuocLau())
                .tenNuocLau(nuocLau.getTenNuocLau())
                .moTa(nuocLau.getMoTa())
                .giaPhuThu(nuocLau.getGiaPhuThu())
                .hinhAnh((nuocLau.getHinhAnh()))
                .build();
    }

    @Override
    public void addNuocLau(NuocLauRequest request, MultipartFile file) {
        NuocLau nuocLau = new NuocLau();
        nuocLau.setTenNuocLau(request.getTenNuocLau());
        nuocLau.setMoTa(request.getMoTa());
        nuocLau.setGiaPhuThu(request.getGiaPhuThu());
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "/app/uploads/";

                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                file.transferTo(dest);

                nuocLau.setHinhAnh(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thất bại");
            }
        }
        nuocLauRepository.save(nuocLau);
    }

    @Override
    public NuocLauResponse updateNuocLau(Long id, NuocLauRequest request, MultipartFile file) {
        NuocLau nuocLau = nuocLauRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nước lẩu"));
        nuocLau.setTenNuocLau(request.getTenNuocLau());
        nuocLau.setMoTa(request.getMoTa());
        nuocLau.setGiaPhuThu(request.getGiaPhuThu());

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "/app/uploads/";

                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                // xóa ảnh cũ (nếu có)
                if (nuocLau.getHinhAnh() != null) {
                    File oldFile = new File(uploadDir + nuocLau.getHinhAnh());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // lưu ảnh mới
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                file.transferTo(dest);

                nuocLau.setHinhAnh(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thất bại");
            }
        }
        nuocLauRepository.save(nuocLau);
        NuocLauResponse res = new NuocLauResponse();
        res.setId(nuocLau.getId());
        res.setTenNuocLau(nuocLau.getTenNuocLau());
        res.setMoTa(nuocLau.getMoTa());
        res.setGiaPhuThu(nuocLau.getGiaPhuThu());
        res.setHinhAnh(nuocLau.getHinhAnh());

        return res;
    }

    @Override
    public List<NuocLauResponse> searchNuocLau(NuocLauRequest req) {
        Specification<NuocLau> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // tìm theo tên
            if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("tenNuocLau")),
                                "%" + req.getKeyword().toLowerCase() + "%"
                        )
                );
            }

            // lọc trạng thái
            if (req.getTrangThaiXoa() != null) {
                predicates.add(
                        cb.equal(root.get("trangThaiXoa"), req.getTrangThaiXoa())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // sắp xếp
        Sort sort = Sort.unsorted();

        if ("asc".equals(req.getSortGia())) {
            sort = Sort.by("giaPhuThu").ascending();
        } else if ("desc".equals(req.getSortGia())) {
            sort = Sort.by("giaPhuThu").descending();
        }

        List<NuocLau> list = nuocLauRepository.findAll(spec, sort);

        return list.stream().map(nl ->
                NuocLauResponse.builder()
                        .id(nl.getId())
                        .maNuocLau(nl.getMaNuocLau())
                        .tenNuocLau(nl.getTenNuocLau())
                        .giaPhuThu(nl.getGiaPhuThu())
                        .moTa(nl.getMoTa())
                        .trangThaiXoa(nl.getTrangThaiXoa())
                        .hinhAnh(nl.getHinhAnh())
                        .build()
        ).toList();
    }

    @Override
    public void chuyenTrangThai(Integer id) {
        Optional<NuocLau> optional = nuocLauRepository.findById(Long.valueOf(id));
        NuocLau nuocLau = optional.orElseThrow();
        nuocLau.setTrangThaiXoa(!Boolean.TRUE.equals(nuocLau.getTrangThaiXoa()));
        nuocLauRepository.save(nuocLau);
    }

}

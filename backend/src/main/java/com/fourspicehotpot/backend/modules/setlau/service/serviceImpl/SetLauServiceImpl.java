package com.fourspicehotpot.backend.modules.setlau.service.serviceImpl;

import com.fourspicehotpot.backend.modules.setlau.service.SetLauService;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SetLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.ThanhPhanRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.dto.response.ThanhPhanResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.NuocLau;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;
import com.fourspicehotpot.backend.modules.setlau.repository.NuocLauRepository;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.setlau.repository.SetThanhPhanRepository;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import com.fourspicehotpot.backend.modules.thucdon.repository.DanhMucRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Service
public class SetLauServiceImpl implements SetLauService {


    @Autowired
    private NuocLauRepository nuocLauRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SetThanhPhanRepository setThanhPhanRepository;


    @Transactional
    @Override
    public void addSetLau(SetLauRequest dto, MultipartFile file) {
        SanPham setLau = new SanPham();
        setLau.setTenSanPham(dto.getTenSanPham());
        setLau.setGia(dto.getGia());
        setLau.setMoTa(dto.getMoTa());
        setLau.setLaSet(true);

        DanhMuc danhMuc = danhMucRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Danh mục Set không tồn tại"));
        setLau.setDanhMuc(danhMuc);

        NuocLau nuocLau = nuocLauRepository.findById(Long.valueOf(dto.getIdNuocLau()))
                .orElseThrow(() -> new RuntimeException("Nước lẩu không tồn tại"));
        setLau.setNuocLau(nuocLau);

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

                setLau.setHinhAnh(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thất bại");
            }
        }
        sanPhamRepository.save(setLau);

        if (dto.getThanhPhans() != null && !dto.getThanhPhans().isEmpty()) {

            for (ThanhPhanRequest tp : dto.getThanhPhans()) {

                SanPham sanPham = sanPhamRepository.findById(Long.valueOf(tp.getIdSanPham()))
                        .orElseThrow(() -> new RuntimeException("Sản phẩm thành phần không tồn tại"));

                SetThanhPhan entity = new SetThanhPhan();
                entity.setSet(setLau);
                entity.setSanPhamThanhPhan(sanPham);
                entity.setSoLuong(tp.getSoLuong());

                setThanhPhanRepository.save(entity);
            }
        }
    }

    @Override
    public SanPhamResponse findSetLauById(Integer id) {

        SanPham sp = sanPhamRepository
                .findByIdAndLaSetTrue(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy set lẩu"));

        List<SetThanhPhan> listThanhPhan =
                setThanhPhanRepository.findBySetIdAndTrangThaiXoaFalse(Long.valueOf(id));

        List<ThanhPhanResponse> thanhPhans = listThanhPhan.stream()
                .map(tp -> ThanhPhanResponse.builder()
                        .idSanPham(tp.getSanPhamThanhPhan().getId())
                        .tenSanPham(tp.getSanPhamThanhPhan().getTenSanPham())
                        .gia(tp.getSanPhamThanhPhan().getGia())
                        .soLuong(tp.getSoLuong())

                        // ảnh của sản phẩm thành phần
                        .hinhAnh(tp.getSanPhamThanhPhan().getHinhAnh())

                        // danh mục của sản phẩm thành phần
                        .tenDanhMuc(tp.getSanPhamThanhPhan().getDanhMuc().getTenDanhMuc())

                        // lấy từ sản phẩm thành phần
                        .giaTri(tp.getSanPhamThanhPhan().getDinhLuong().getGiaTri())
                        .tenDonVi(tp.getSanPhamThanhPhan().getDinhLuong().getDonVi().getTenDonVi())
                        .build())
                .toList();

        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(sp.getGia())
                .moTa(sp.getMoTa())
                .laSet(sp.getLaSet())
                // Nuocws lẩu
                .idNuocLau(sp.getNuocLau().getId())
                .tenNuocLau(sp.getNuocLau().getTenNuocLau())
                .giaPhuThu(sp.getNuocLau().getGiaPhuThu())
                // danh mục
                .idDanhMuc(sp.getDanhMuc().getId())
                .tenDanhMuc(sp.getDanhMuc().getTenDanhMuc())

                // ảnh đại diện
                .hinhAnh(sp.getHinhAnh())

                // thành phần set
                .thanhPhans(thanhPhans)

                .build();
    }

    @Transactional
    @Override
    public void updateSetLau(Integer id, SetLauRequest dto, MultipartFile file) {

        SanPham setLau = sanPhamRepository.findByIdAndLaSetTrue(id)
                .orElseThrow(() -> new RuntimeException("Set lẩu không tồn tại"));

        setLau.setTenSanPham(dto.getTenSanPham());
        setLau.setGia(dto.getGia());
        setLau.setMoTa(dto.getMoTa());

        NuocLau nuocLau = nuocLauRepository.findById(Long.valueOf(dto.getIdNuocLau()))
                .orElseThrow(() -> new RuntimeException("Nước lẩu không tồn tại"));
        setLau.setNuocLau(nuocLau);

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "/app/uploads/";

                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                Path path = Paths.get(uploadDir, fileName);

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                setLau.setHinhAnh(fileName);

            } catch (Exception e) {
                e.printStackTrace(); // in lỗi thật
                throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
            }
        }
        sanPhamRepository.save(setLau);
        System.out.println("ĐÃ SAVE DB, HÌNH ẢNH: " + setLau.getHinhAnh());

        // Xoa thanh phan cu
        List<SetThanhPhan> oldThanhPhan =
                setThanhPhanRepository.findBySetIdAndTrangThaiXoaFalse(setLau.getId());
        for (SetThanhPhan tp : oldThanhPhan) {
            tp.setTrangThaiXoa(true);
            setThanhPhanRepository.save(tp);
        }

        // Them lai thanh phan moi
        if (dto.getThanhPhans() != null && !dto.getThanhPhans().isEmpty()) {
            for (ThanhPhanRequest tp : dto.getThanhPhans()) {
                SanPham sanPham = sanPhamRepository.findById(Long.valueOf(tp.getIdSanPham()))
                        .orElseThrow(() -> new RuntimeException("Sản phẩm thành phần không tồn tại"));
                SetThanhPhan entity = new SetThanhPhan();
                entity.setSet(setLau);
                entity.setSanPhamThanhPhan(sanPham);
                entity.setSoLuong(tp.getSoLuong());

                setThanhPhanRepository.save(entity);
            }
        }
    }


}

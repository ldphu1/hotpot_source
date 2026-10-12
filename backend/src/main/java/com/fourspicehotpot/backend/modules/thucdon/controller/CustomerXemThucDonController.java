package com.fourspicehotpot.backend.modules.thucdon.controller;

import com.fourspicehotpot.backend.modules.setlau.dto.request.NuocLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.NuocLauResponse;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SetThanhPhan;
import com.fourspicehotpot.backend.modules.setlau.service.NuocLauService;
import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import com.fourspicehotpot.backend.modules.setlau.service.SetLauService;
import com.fourspicehotpot.backend.modules.setlau.service.SetThanhPhanService;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.service.CustomerXemThucDonService;
import com.fourspicehotpot.backend.modules.thucdon.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/public")
@CrossOrigin(originPatterns = "http://localhost:*")
public class CustomerXemThucDonController {

    @Autowired
    private SetThanhPhanService setThanhPhanService;
    @Autowired
    private SetLauService setLauService;
    @Autowired
    private SanPhamService sanPhamService;
    @Autowired
    private NuocLauService nuocLauService;
    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private CustomerXemThucDonService customerXemThucDonService;


    ///  /// SẢN PHẨM /// ///
    @GetMapping("/xem-thuc-don")
    public List<SanPhamResponse> getSanPham() {
        return sanPhamService.getAllSanPham();
    }

    @GetMapping("/thuc-don-mon-an")
    public ResponseEntity<?> getAllMonLe() {
        return ResponseEntity.ok(sanPhamService.getAllMonLe());
    }

    @GetMapping("/mon-an/detail/{id}")
    public SanPhamResponse getSanPhamById(@PathVariable Integer id) {
        return sanPhamService.findById(id);
    }

    @GetMapping("/thuc-don/by-danh-muc/{idDanhMuc}")
    public List<SanPhamResponse> getByDanhMuc(@PathVariable Integer idDanhMuc) {
        return sanPhamService.findSanPhamByDanhMuc(idDanhMuc);
    }

    @GetMapping("/mon-an/search")
    public List<SanPhamResponse> searchSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortGia
    ) {

        SanPhamSearchRequest request = new SanPhamSearchRequest();
        request.setKeyword(keyword);
        request.setSortGia(sortGia);

        return customerXemThucDonService.searchSanPham(request);
    }


    ///  /// NƯỚC LẨU /// ///
    @GetMapping("/nuoc-lau/search")
    public List<NuocLauResponse> searchNuocLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) String sortGia
    ) {

        NuocLauRequest req = new NuocLauRequest();
        req.setKeyword(keyword);
        req.setTrangThaiXoa(trangThai);
        req.setSortGia(sortGia);

        return nuocLauService.searchNuocLau(req);
    }

    @GetMapping("/nuoc-lau/detail/{id}")
    public ResponseEntity<NuocLauResponse> detailNuocLau(@PathVariable Long id) {
        return ResponseEntity.ok(nuocLauService.getNuocLauById(id));
    }

    @GetMapping("/loai-lau")
    public List<NuocLauResponse> hienThi() {

        return nuocLauService.getAllNuocLau();
    }


    /// /// SET LẨU /// ///

    @GetMapping("/set-lau")
    public List<SanPhamResponse> getSetLau() {
        return sanPhamService.getAllSetLau();
    }

    @GetMapping("/set-lau-filter")
    public List<SanPhamResponse> getSetLauFilter(
            @RequestParam(required = false) Long idNuocLau

    ) {
        return customerXemThucDonService.searchSetLauByNuocLau(idNuocLau);
    }

    @GetMapping("/set-lau-dgg")
    public List<SanPhamResponse> getAllSanPhamDotGiamGia() {
        return sanPhamService.getSanPhamDotGiamGia();
    }

    @GetMapping("/thuc-don/client")
    public List<SanPhamResponse> getAllSanPhamDotGiamGiaByDanhMuc(
            @RequestParam(required = false) Integer idDanhMuc
    ) {
        return sanPhamService.getSanPhamDotGiamGiaByDanhmuc(idDanhMuc);
    }

    @GetMapping("/san-pham")
    public ResponseEntity<?> getSanPham(@RequestParam Boolean laSet) {
        return ResponseEntity.ok(sanPhamService.getSanPhamTheoLoai(laSet));
    }

    @GetMapping("/set-lau/detail/{id}")
    public SanPhamResponse getSetLauById(@PathVariable("id") Integer id) {
        return setLauService.findSetLauById(id);
    }

    @GetMapping("/set-lau/search")
    public List<SanPhamResponse> searchSetLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idNuocLau,
            @RequestParam(required = false) Boolean trangThaiXoa,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {

        return sanPhamService.searchSetLau(
                keyword,
                idNuocLau,
                trangThaiXoa,
                minPrice,
                maxPrice
        );
    }

    /// /// SET THÀNH PHẦN /// ///
    @GetMapping("/set-thanh-phan/hien-thi")
    public List<SetThanhPhan> findAll() {
        return setThanhPhanService.findAllSetThanhPhan();
    }

    @GetMapping("/set-thanh-phan/detail/{id}")
    public SetThanhPhan findById(@PathVariable Long id) {

        return setThanhPhanService.findById(id);
    }

    /// /// DANH MỤC /// ///
    @GetMapping("/loai-mon-an")
    public List<DanhMucResponse> hienThi(Model model) {
        return danhMucService.findAllDanhMucHoatDong();
    }

}

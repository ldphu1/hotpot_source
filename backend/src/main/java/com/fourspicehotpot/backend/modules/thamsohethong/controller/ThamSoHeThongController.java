package com.fourspicehotpot.backend.modules.thamsohethong.controller;

import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import com.fourspicehotpot.backend.modules.thamsohethong.dto.request.ThamSoHeThongRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/tham-so-he-thong")
public class ThamSoHeThongController {
    @Autowired
    private ThamSoHeThongService thamSoHeThongService;

    @GetMapping("/hien-thi")
    public ResponseEntity<?> getAllThamSo() {
        return ResponseEntity.ok(thamSoHeThongService.getAllThamSo());
    }

    @GetMapping("/get/{code}")
    public ResponseEntity<String> get(@PathVariable String code) {
        return ResponseEntity.ok(thamSoHeThongService.get(code));
    }

    @GetMapping("/get-int/{code}")
    public ResponseEntity<Integer> getInt(@PathVariable String code) {
        return ResponseEntity.ok(thamSoHeThongService.getInt(code));
    }

    @GetMapping("/get-double/{code}")
    public ResponseEntity<Double> getDouble(@PathVariable String code) {
        return ResponseEntity.ok(thamSoHeThongService.getDouble(code));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(thamSoHeThongService.getDetail(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> create(@RequestBody ThamSoHeThongRequest request) {
        return ResponseEntity.ok(thamSoHeThongService.create(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody ThamSoHeThongRequest request) {
        return ResponseEntity.ok(thamSoHeThongService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        thamSoHeThongService.delete(id);
        return ResponseEntity.ok("Xóa tham số hệ thống thành công");
    }
}

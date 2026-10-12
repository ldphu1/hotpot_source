package com.fourspicehotpot.backend.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity {

    @CreatedDate
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @LastModifiedDate
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @CreatedBy
    @Column(name = "nguoi_tao", updatable = false)
    private String nguoiTao;

    @LastModifiedBy
    @Column(name = "nguoi_cap_nhat")
    private String nguoiCapNhat;

}

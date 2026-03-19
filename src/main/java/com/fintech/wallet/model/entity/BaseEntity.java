package com.fintech.wallet.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@MappedSuperclass // บอก Hibernate ว่าคลาสนี้เป็นแค่แม่แบบ ไม่ต้องสร้าง Table เอง
@Getter
@Setter
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", insertable = false, updatable = false)
    private boolean isDeleted = false; // Default เป็น false เสมอ
}
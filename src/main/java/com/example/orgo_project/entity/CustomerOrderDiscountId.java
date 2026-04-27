package com.example.orgo_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDiscountId implements Serializable {
    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "id_ma_giam_gia")
    private Integer discountCodeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerOrderDiscountId that)) {
            return false;
        }
        return Objects.equals(orderId, that.orderId) && Objects.equals(discountCodeId, that.discountCodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, discountCodeId);
    }
}

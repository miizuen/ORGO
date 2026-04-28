package com.example.orgo_project.dto;

import com.example.orgo_project.entity.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressDTO {
    private Integer id;
    private String recipientName;
    private String recipientPhone;
    private String provinceOrCity;
    private Boolean defaultAddress;
    private String addressType;
    private String detailedAddress;

    public static ShippingAddressDTO fromEntity(ShippingAddress address) {
        ShippingAddressDTO dto = new ShippingAddressDTO();
        dto.setId(address.getId());
        dto.setRecipientName(address.getRecipientName());
        dto.setRecipientPhone(address.getRecipientPhone());
        dto.setProvinceOrCity(address.getProvinceOrCity());
        dto.setDefaultAddress(address.getDefaultAddress());
        dto.setAddressType(address.getAddressType());
        dto.setDetailedAddress(address.getDetailedAddress());
        return dto;
    }
}

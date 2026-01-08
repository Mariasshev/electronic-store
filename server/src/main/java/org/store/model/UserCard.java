package org.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCard {
    private Long id;
    private Long userId;
    private String cardHolder;
    private String brand;
    private String last4;
    private String expiryDate;
}
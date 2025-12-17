package com.microservices.api.model;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private int price;
    private int quantity;

}

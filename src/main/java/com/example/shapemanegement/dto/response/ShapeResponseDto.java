package com.example.shapemanegement.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShapeResponseDto {
    private Long id;
    private String name;
    private String type;
    private String coordinates;
    private Double centerX;
    private Double centerY;
    private Double radius;
}

package com.example.shapemanegement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShapeRequestDto {

    @NotBlank(message = "Shape name cannot be empty")
    private String name;

    @NotBlank(message = "Shape type cannot be empty")
    @Pattern(regexp = "rectangle|triangle|circle|polygon", message = "Invalid shape type. Must be rectangle, triangle, circle, or polygon.")
    private String type;

    // For polygons
    private String coordinates;

    // For circles
    private Double centerX;
    private Double centerY;

    @Positive(message = "Radius must be a positive value for circles.")
    private Double radius;
}

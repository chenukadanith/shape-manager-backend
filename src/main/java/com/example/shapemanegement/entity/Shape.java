package com.example.shapemanegement.entity;

import com.example.shapemanegement.converter.StringToJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "shapes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;


    @Column(name = "coordinates", columnDefinition = "JSON")
    @Convert(converter = StringToJsonConverter.class)
    private String coordinates;

    // For circles
    @Column(name = "center_x")
    private Double centerX;

    @Column(name = "center_y")
    private Double centerY;

    @Column(name = "radius")
    private Double radius;
}
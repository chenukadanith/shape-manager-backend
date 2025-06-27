package com.example.shapemanegement.controller;


import com.example.shapemanegement.dto.request.ShapeRequestDto;
import com.example.shapemanegement.dto.response.ShapeResponseDto;
import com.example.shapemanegement.service.IShapeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shapes")
public class ShapeController {

    private final IShapeService shapeService;

    public ShapeController(IShapeService shapeService) {
        this.shapeService = shapeService;
    }

    @GetMapping
    public ResponseEntity<List<ShapeResponseDto>> getAllShapes() {
        List<ShapeResponseDto> shapes = shapeService.findAllShapes();
        return ResponseEntity.ok(shapes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShapeResponseDto> getShapeById(@PathVariable Long id) {
        ShapeResponseDto shape = shapeService.findShapeById(id);
        return ResponseEntity.ok(shape);
    }

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')") // Requires ADMIN role for create
    public ResponseEntity<ShapeResponseDto> createShape(@Valid @RequestBody ShapeRequestDto shapeDto) {
        ShapeResponseDto createdShape = shapeService.createShape(shapeDto);
        return new ResponseEntity<>(createdShape, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")

    public ResponseEntity<ShapeResponseDto> updateShape(@PathVariable Long id, @Valid @RequestBody ShapeRequestDto shapeDto) {
        ShapeResponseDto updatedShape = shapeService.updateShape(id, shapeDto);
        return ResponseEntity.ok(updatedShape);
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteShape(@PathVariable Long id) {
        shapeService.deleteShape(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/overlaps") // Example endpoint for getting overlapping IDs
    public ResponseEntity<List<Long>> getOverlappingShapeIds() {
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();
        return ResponseEntity.ok(overlappingIds);
    }

}
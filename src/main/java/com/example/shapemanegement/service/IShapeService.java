package com.example.shapemanegement.service;

import com.example.shapemanegement.dto.request.ShapeRequestDto;
import com.example.shapemanegement.dto.response.ShapeResponseDto;

import java.util.List;

public interface IShapeService {
    List<ShapeResponseDto> findAllShapes();

    ShapeResponseDto findShapeById(Long id);

    ShapeResponseDto createShape(ShapeRequestDto requestDto);

    ShapeResponseDto updateShape(Long id, ShapeRequestDto requestDto);

    void deleteShape(Long id);


     List<Long> findOverlappingShapeIds();
}

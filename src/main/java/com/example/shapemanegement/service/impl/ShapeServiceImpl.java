package com.example.shapemanegement.service.impl;


import com.example.shapemanegement.dto.request.ShapeRequestDto;
import com.example.shapemanegement.dto.response.ShapeResponseDto;
import com.example.shapemanegement.entity.Shape;
import com.example.shapemanegement.exception.ResourceNotFoundException;
import com.example.shapemanegement.exception.ValidationException;
import com.example.shapemanegement.repository.ShapeRepository;
import com.example.shapemanegement.service.IShapeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShapeServiceImpl implements IShapeService {

    private final ShapeRepository shapeRepository;

    public ShapeServiceImpl(ShapeRepository shapeRepository) {
        this.shapeRepository = shapeRepository;
    }

    // --- Utility for Coordinate Validation ---
    private void validateCoordinates(String type, String coordinates, Double centerX, Double centerY, Double radius) {
        if ("circle".equalsIgnoreCase(type)) {
            if (centerX == null || centerY == null || radius == null) {
                throw new ValidationException("For circle, centerX, centerY, and radius are required.");
            }
            if (radius <= 0) {
                throw new ValidationException("Radius must be positive.");
            }
        } else {
            if (coordinates == null || coordinates.trim().isEmpty()) {
                throw new ValidationException("Coordinates string is required for " + type + ".");
            }
            Pattern coordPattern = Pattern.compile("^\\s*\\d+,\\d+(;\\s*\\d+,\\d+)*\\s*$");
            if (!coordPattern.matcher(coordinates).matches()) {
                throw new ValidationException("Coordinates format invalid. Expected 'x1,y1;x2,y2;...'");
            }
            String[] points = coordinates.split(";");
            if ("triangle".equalsIgnoreCase(type) && points.length < 3) {
                throw new ValidationException("Triangle requires at least 3 points.");
            }
            if (("rectangle".equalsIgnoreCase(type) || "polygon".equalsIgnoreCase(type)) && points.length < 3) {
                throw new ValidationException("Rectangle/Polygon requires at least 3 points.");
            }
        }
    }

    // --- CRUD Operations (Implementations of IShapeService methods) ---

    @Override
    public List<ShapeResponseDto> findAllShapes() {
        return shapeRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ShapeResponseDto findShapeById(Long id) {
        Shape shape = shapeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shape not found with id: " + id));
        return mapToResponseDto(shape);
    }

    @Override
    @Transactional
    public ShapeResponseDto createShape(ShapeRequestDto requestDto) {
        if (shapeRepository.existsByName(requestDto.getName())) {
            throw new ValidationException("Shape with name '" + requestDto.getName() + "' already exists.");
        }
        validateCoordinates(requestDto.getType(), requestDto.getCoordinates(),
                requestDto.getCenterX(), requestDto.getCenterY(), requestDto.getRadius());

        Shape shape = mapToEntity(requestDto);
        Shape savedShape = shapeRepository.save(shape);
        return mapToResponseDto(savedShape);
    }

    @Override
    @Transactional
    public ShapeResponseDto updateShape(Long id, ShapeRequestDto requestDto) {
        Shape existingShape = shapeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shape not found with id: " + id));

        if (shapeRepository.existsByNameAndIdNot(requestDto.getName(), id)) {
            throw new ValidationException("Shape with name '" + requestDto.getName() + "' already exists.");
        }
        validateCoordinates(requestDto.getType(), requestDto.getCoordinates(),
                requestDto.getCenterX(), requestDto.getCenterY(), requestDto.getRadius());

        existingShape.setName(requestDto.getName());
        existingShape.setType(requestDto.getType());
        existingShape.setCoordinates(requestDto.getCoordinates());
        existingShape.setCenterX(requestDto.getCenterX());
        existingShape.setCenterY(requestDto.getCenterY());
        existingShape.setRadius(requestDto.getRadius());

        Shape updatedShape = shapeRepository.save(existingShape);
        return mapToResponseDto(updatedShape);
    }

    @Override
    @Transactional
    public void deleteShape(Long id) {
        if (!shapeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shape not found with id: " + id);
        }
        shapeRepository.deleteById(id);
    }

    //  Helper methods for mapping -
    private ShapeResponseDto mapToResponseDto(Shape shape) {
        return new ShapeResponseDto(
                shape.getId(),
                shape.getName(),
                shape.getType(),
                shape.getCoordinates(),
                shape.getCenterX(),
                shape.getCenterY(),
                shape.getRadius()
        );
    }

    private Shape mapToEntity(ShapeRequestDto dto) {
        Shape shape = new Shape();
        shape.setName(dto.getName());
        shape.setType(dto.getType());
        shape.setCoordinates(dto.getCoordinates());
        shape.setCenterX(dto.getCenterX());
        shape.setCenterY(dto.getCenterY());
        shape.setRadius(dto.getRadius());
        return shape;
    }

    // TODO: Overlap detection method implementation will go here
    /*
    @Override
    public List<Long> findOverlappingShapeIds() {
        // Implementation for overlap detection using JTS
        // 1. Fetch all shapes from shapeRepository.findAll()
        // 2. Convert them to JTS Geometry objects
        // 3. Perform pairwise overlap checks with bounding box optimization
        // 4. Return the IDs of shapes involved in genuine overlaps
        return new ArrayList<>();
    }
    */
}
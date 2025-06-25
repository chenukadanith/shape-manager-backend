package com.example.shapemanegement.service.impl;

import com.example.shapemanegement.dto.request.ShapeRequestDto;
import com.example.shapemanegement.dto.response.ShapeResponseDto;
import com.example.shapemanegement.entity.Shape;
import com.example.shapemanegement.exception.ResourceNotFoundException;
import com.example.shapemanegement.exception.ValidationException;
import com.example.shapemanegement.repository.ShapeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShapeService Unit Tests")
class ShapeServiceImplTest {

    @Mock
    private ShapeRepository shapeRepository;

    @InjectMocks
    private ShapeServiceImpl shapeService;

    private Shape circle1;
    private Shape circle2;
    private Shape triangle1;
    private Shape rectangle1;

    @BeforeEach
    void setUp() {
        // Create test shapes
        circle1 = createCircle(1L, "Circle1", 0.0, 0.0, 5.0);
        circle2 = createCircle(2L, "Circle2", 8.0, 0.0, 3.0);
        triangle1 = createTriangle(3L, "Triangle1", "0,0;10,0;5,10");
        rectangle1 = createRectangle(4L, "Rectangle1", "20,20;30,20;30,30;20,30");
    }

    // --- OVERLAP DETECTION TESTS ---

    @Test
    @DisplayName("Should detect overlapping circles")
    void shouldDetectOverlappingCircles() {
        // Given: Two overlapping circles
        Shape overlappingCircle = createCircle(5L, "OverlapCircle", 3.0, 0.0, 4.0);
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(circle1, overlappingCircle));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).hasSize(2);
        assertThat(overlappingIds).containsExactlyInAnyOrder(1L, 5L);
    }

    @Test
    @DisplayName("Should detect non-overlapping circles")
    void shouldDetectNonOverlappingCircles() {
        // Given: Two non-overlapping circles
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(circle1, circle2));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).isEmpty();
    }

    @Test
    @DisplayName("Should detect circle-polygon overlap")
    void shouldDetectCirclePolygonOverlap() {
        // Given: Circle overlapping with triangle
        Shape overlappingCircle = createCircle(6L, "OverlapCircle", 5.0, 3.0, 4.0);
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(triangle1, overlappingCircle));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).hasSize(2);
        assertThat(overlappingIds).containsExactlyInAnyOrder(3L, 6L);
    }

    @Test
    @DisplayName("Should detect polygon-polygon overlap")
    void shouldDetectPolygonPolygonOverlap() {
        // Given: Two overlapping rectangles
        Shape overlappingRect = createRectangle(7L, "OverlapRect", "25,25;35,25;35,35;25,35");
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(rectangle1, overlappingRect));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).hasSize(2);
        assertThat(overlappingIds).containsExactlyInAnyOrder(4L, 7L);
    }

    @Test
    @DisplayName("Should handle empty shape list")
    void shouldHandleEmptyShapeList() {
        // Given: Empty repository
        when(shapeRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).isEmpty();
    }

    @Test
    @DisplayName("Should handle single shape")
    void shouldHandleSingleShape() {
        // Given: Only one shape
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(circle1));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).isEmpty();
    }

    @Test
    @DisplayName("Should detect multiple overlapping shapes")
    void shouldDetectMultipleOverlappingShapes() {
        // Given: Multiple shapes with overlaps
        Shape circle3 = createCircle(8L, "Circle3", 2.0, 2.0, 3.0);
        Shape circle4 = createCircle(9L, "Circle4", 4.0, 4.0, 3.0);
        when(shapeRepository.findAll()).thenReturn(Arrays.asList(circle1, circle3, circle4));

        // When
        List<Long> overlappingIds = shapeService.findOverlappingShapeIds();

        // Then
        assertThat(overlappingIds).hasSize(3);
        assertThat(overlappingIds).containsExactlyInAnyOrder(1L, 8L, 9L);
    }

    // --- CRUD OPERATION TESTS ---

    @Test
    @DisplayName("Should create shape successfully")
    void shouldCreateShape() {
        // Given
        ShapeRequestDto requestDto = new ShapeRequestDto();
        requestDto.setName("TestCircle");
        requestDto.setType("circle");
        requestDto.setCenterX(10.0);
        requestDto.setCenterY(10.0);
        requestDto.setRadius(5.0);

        Shape savedShape = createCircle(10L, "TestCircle", 10.0, 10.0, 5.0);

        when(shapeRepository.existsByName("TestCircle")).thenReturn(false);
        when(shapeRepository.save(any(Shape.class))).thenReturn(savedShape);

        // When
        ShapeResponseDto response = shapeService.createShape(requestDto);

        // Then
        assertThat(response.getName()).isEqualTo("TestCircle");
        assertThat(response.getType()).isEqualTo("circle");
        assertThat(response.getCenterX()).isEqualTo(10.0);
        assertThat(response.getCenterY()).isEqualTo(10.0);
        assertThat(response.getRadius()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate shape")
    void shouldThrowExceptionWhenCreatingDuplicateShape() {
        // Given
        ShapeRequestDto requestDto = new ShapeRequestDto();
        requestDto.setName("DuplicateShape");
        requestDto.setType("circle");
        requestDto.setCenterX(10.0);
        requestDto.setCenterY(10.0);
        requestDto.setRadius(5.0);

        when(shapeRepository.existsByName("DuplicateShape")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> shapeService.createShape(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should validate circle coordinates")
    void shouldValidateCircleCoordinates() {
        // Given
        ShapeRequestDto requestDto = new ShapeRequestDto();
        requestDto.setName("InvalidCircle");
        requestDto.setType("circle");
        requestDto.setCenterX(10.0);
        requestDto.setCenterY(10.0);
        requestDto.setRadius(-5.0); // Invalid radius

        when(shapeRepository.existsByName("InvalidCircle")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> shapeService.createShape(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Radius must be positive");
    }

    @Test
    @DisplayName("Should find shape by ID")
    void shouldFindShapeById() {
        // Given
        when(shapeRepository.findById(1L)).thenReturn(Optional.of(circle1));

        // When
        ShapeResponseDto response = shapeService.findShapeById(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Circle1");
        assertThat(response.getType()).isEqualTo("circle");
    }

    @Test
    @DisplayName("Should throw exception when shape not found")
    void shouldThrowExceptionWhenShapeNotFound() {
        // Given
        when(shapeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> shapeService.findShapeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shape not found with id: 999");
    }

    @Test
    @DisplayName("Should delete shape successfully")
    void shouldDeleteShape() {
        // Given
        when(shapeRepository.existsById(1L)).thenReturn(true);

        // When
        shapeService.deleteShape(1L);

        // Then
        verify(shapeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent shape")
    void shouldThrowExceptionWhenDeletingNonExistentShape() {
        // Given
        when(shapeRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> shapeService.deleteShape(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shape not found with id: 999");
    }

    // --- HELPER METHODS ---

    private Shape createCircle(Long id, String name, Double centerX, Double centerY, Double radius) {
        Shape shape = new Shape();
        shape.setId(id);
        shape.setName(name);
        shape.setType("circle");
        shape.setCenterX(centerX);
        shape.setCenterY(centerY);
        shape.setRadius(radius);
        return shape;
    }

    private Shape createTriangle(Long id, String name, String coordinates) {
        Shape shape = new Shape();
        shape.setId(id);
        shape.setName(name);
        shape.setType("triangle");
        shape.setCoordinates(coordinates);
        return shape;
    }

    private Shape createRectangle(Long id, String name, String coordinates) {
        Shape shape = new Shape();
        shape.setId(id);
        shape.setName(name);
        shape.setType("rectangle");
        shape.setCoordinates(coordinates);
        return shape;
    }
}
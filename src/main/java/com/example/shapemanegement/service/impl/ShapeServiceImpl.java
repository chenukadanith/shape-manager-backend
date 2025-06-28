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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShapeServiceImpl implements IShapeService {

    private final ShapeRepository shapeRepository;

    public ShapeServiceImpl(ShapeRepository shapeRepository) {
        this.shapeRepository = shapeRepository;
    }

    // Utility for Coordinate Validation
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

    //  Helper methods for mapping
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

    // --- OVERLAP DETECTION IMPLEMENTATION ---

    @Override
    public List<Long> findOverlappingShapeIds() {
        List<Shape> allShapes = shapeRepository.findAll();
        Set<Long> overlappingShapeIds = new HashSet<>();

        // Check all pairs of shapes for overlaps
        for (int i = 0; i < allShapes.size(); i++) {
            for (int j = i + 1; j < allShapes.size(); j++) {
                Shape shape1 = allShapes.get(i);
                Shape shape2 = allShapes.get(j);

                if (shapesOverlap(shape1, shape2)) {
                    overlappingShapeIds.add(shape1.getId());
                    overlappingShapeIds.add(shape2.getId());
                }
            }
        }

        return new ArrayList<>(overlappingShapeIds);
    }

    private boolean shapesOverlap(Shape shape1, Shape shape2) {
        // Early bounding box check for performance optimization
        if (!boundingBoxesOverlap(shape1, shape2)) {
            return false;
        }

        // Detailed overlap detection based on shape types
        if (isCircle(shape1) && isCircle(shape2)) {
            return circleCircleOverlap(shape1, shape2);
        } else if (isCircle(shape1) && isPolygon(shape2)) {
            return circlePolygonOverlap(shape1, shape2);
        } else if (isPolygon(shape1) && isCircle(shape2)) {
            return circlePolygonOverlap(shape2, shape1);
        } else if (isPolygon(shape1) && isPolygon(shape2)) {
            return polygonPolygonOverlap(shape1, shape2);
        }

        return false;
    }

    // Shape type helper methods
    private boolean isCircle(Shape shape) {
        return "circle".equalsIgnoreCase(shape.getType());
    }

    private boolean isPolygon(Shape shape) {
        String type = shape.getType().toLowerCase();
        return "triangle".equals(type) || "rectangle".equals(type) || "polygon".equals(type);
    }

    // Bounding Box Optimization
    private boolean boundingBoxesOverlap(Shape shape1, Shape shape2) {
        double[] bounds1 = getBoundingBox(shape1);
        double[] bounds2 = getBoundingBox(shape2);


        return !(bounds1[2] < bounds2[0] ||
                bounds2[2] < bounds1[0] ||
                bounds1[3] < bounds2[1] ||
                bounds2[3] < bounds1[1]);
    }

    private double[] getBoundingBox(Shape shape) {
        if (isCircle(shape)) {
            double minX = shape.getCenterX() - shape.getRadius();
            double minY = shape.getCenterY() - shape.getRadius();
            double maxX = shape.getCenterX() + shape.getRadius();
            double maxY = shape.getCenterY() + shape.getRadius();
            return new double[]{minX, minY, maxX, maxY};
        } else {
            List<Point> points = parseCoordinates(shape.getCoordinates());
            double minX = points.stream().mapToDouble(Point::getX).min().orElse(0);
            double minY = points.stream().mapToDouble(Point::getY).min().orElse(0);
            double maxX = points.stream().mapToDouble(Point::getX).max().orElse(0);
            double maxY = points.stream().mapToDouble(Point::getY).max().orElse(0);
            return new double[]{minX, minY, maxX, maxY};
        }
    }

    // Circle-Circle Overlap Detection
    private boolean circleCircleOverlap(Shape circle1, Shape circle2) {
        double dx = circle1.getCenterX() - circle2.getCenterX();
        double dy = circle1.getCenterY() - circle2.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double radiusSum = circle1.getRadius() + circle2.getRadius();


        return distance < radiusSum;
    }

    // Circle-Polygon Overlap Detection
    private boolean circlePolygonOverlap(Shape circle, Shape polygon) {
        List<Point> polygonPoints = parseCoordinates(polygon.getCoordinates());
        Point center = new Point(circle.getCenterX(), circle.getCenterY());
        double radius = circle.getRadius();

        // Check if circle center is inside polygon
        if (isPointInPolygon(center, polygonPoints)) {
            return true;
        }

        // Check if any polygon vertex is inside circle
        for (Point vertex : polygonPoints) {
            double distance = Math.sqrt(Math.pow(vertex.getX() - center.getX(), 2) +
                    Math.pow(vertex.getY() - center.getY(), 2));
            if (distance < radius) {
                return true;
            }
        }

        // Check if circle intersects any polygon edge
        for (int i = 0; i < polygonPoints.size(); i++) {
            Point p1 = polygonPoints.get(i);
            Point p2 = polygonPoints.get((i + 1) % polygonPoints.size());
            if (distanceFromPointToLineSegment(center, p1, p2) < radius) {
                return true;
            }
        }

        return false;
    }

    // Polygon-Polygon Overlap Detection
    private boolean polygonPolygonOverlap(Shape polygon1, Shape polygon2) {
        List<Point> points1 = parseCoordinates(polygon1.getCoordinates());
        List<Point> points2 = parseCoordinates(polygon2.getCoordinates());

        // Check if any vertex of polygon1 is inside polygon2
        for (Point point : points1) {
            if (isPointInPolygon(point, points2)) {
                return true;
            }
        }

        // Check if any vertex of polygon2 is inside polygon1
        for (Point point : points2) {
            if (isPointInPolygon(point, points1)) {
                return true;
            }
        }

        // Check for edge intersections
        for (int i = 0; i < points1.size(); i++) {
            Point p1 = points1.get(i);
            Point p2 = points1.get((i + 1) % points1.size());

            for (int j = 0; j < points2.size(); j++) {
                Point p3 = points2.get(j);
                Point p4 = points2.get((j + 1) % points2.size());

                if (lineSegmentsIntersect(p1, p2, p3, p4)) {
                    return true;
                }
            }
        }

        return false;
    }

    // --- GEOMETRIC HELPER METHODS ---

    // Coordinate parsing
    private List<Point> parseCoordinates(String coordinates) {
        try {
            String[] pairs = coordinates.split(";");
            List<Point> points = new ArrayList<>();

            for (String pair : pairs) {
                String[] xy = pair.trim().split(",");
                if (xy.length != 2) {
                    throw new IllegalArgumentException("Invalid coordinate format");
                }
                double x = Double.parseDouble(xy[0].trim());
                double y = Double.parseDouble(xy[1].trim());
                points.add(new Point(x, y));
            }

            return points;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate values", e);
        }
    }

    // Point-in-polygon test using ray casting algorithm
    private boolean isPointInPolygon(Point point, List<Point> polygon) {
        int intersections = 0;
        int n = polygon.size();

        for (int i = 0; i < n; i++) {
            Point p1 = polygon.get(i);
            Point p2 = polygon.get((i + 1) % n);

            if (rayIntersectsEdge(point, p1, p2)) {
                intersections++;
            }
        }

        return (intersections % 2) == 1;
    }

    private boolean rayIntersectsEdge(Point point, Point edgeStart, Point edgeEnd) {
        if (edgeStart.getY() > point.getY() != edgeEnd.getY() > point.getY()) {
            double intersectionX = (edgeEnd.getX() - edgeStart.getX()) *
                    (point.getY() - edgeStart.getY()) /
                    (edgeEnd.getY() - edgeStart.getY()) + edgeStart.getX();
            if (point.getX() < intersectionX) {
                return true;
            }
        }
        return false;
    }

    // Distance from point to line segment
    private double distanceFromPointToLineSegment(Point point, Point lineStart, Point lineEnd) {
        double A = point.getX() - lineStart.getX();
        double B = point.getY() - lineStart.getY();
        double C = lineEnd.getX() - lineStart.getX();
        double D = lineEnd.getY() - lineStart.getY();

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;

        if (lenSq == 0) {
            return Math.sqrt(A * A + B * B);
        }

        double param = dot / lenSq;

        double xx, yy;
        if (param < 0) {
            xx = lineStart.getX();
            yy = lineStart.getY();
        } else if (param > 1) {
            xx = lineEnd.getX();
            yy = lineEnd.getY();
        } else {
            xx = lineStart.getX() + param * C;
            yy = lineStart.getY() + param * D;
        }

        double dx = point.getX() - xx;
        double dy = point.getY() - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Line segment intersection test
    private boolean lineSegmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        double d1 = crossProduct(p4.getX() - p3.getX(), p4.getY() - p3.getY(), p1.getX() - p3.getX(), p1.getY() - p3.getY());
        double d2 = crossProduct(p4.getX() - p3.getX(), p4.getY() - p3.getY(), p2.getX() - p3.getX(), p2.getY() - p3.getY());
        double d3 = crossProduct(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p3.getX() - p1.getX(), p3.getY() - p1.getY());
        double d4 = crossProduct(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p4.getX() - p1.getX(), p4.getY() - p1.getY());

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        return false;
    }

    private double crossProduct(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    // Inner class for Point representation
    private static class Point {
        private final double x;
        private final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
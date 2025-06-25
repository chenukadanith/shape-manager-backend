package com.example.shapemanegement.repository;

import com.example.shapemanegement.entity.Shape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShapeRepository  extends JpaRepository<Shape,Long> {
    Optional<Shape> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

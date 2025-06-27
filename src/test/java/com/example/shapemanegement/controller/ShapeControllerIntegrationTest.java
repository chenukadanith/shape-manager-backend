package com.example.shapemanegement.controller;

import com.example.shapemanegement.dto.request.ShapeRequestDto;
import com.example.shapemanegement.entity.Shape;
import com.example.shapemanegement.repository.ShapeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShapeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShapeRepository shapeRepository;

    private Long rectangleId;
    private Long circleId;
    private Long polygonId;
    private Long overlappingRect1Id;
    private Long overlappingRect2Id;

    @BeforeEach
    void setup() {
        shapeRepository.deleteAll();

        Shape savedRectangle = shapeRepository.save(new Shape(null, "MyRectangle", "rectangle", "0,0,10,10", null, null, null));
        rectangleId = savedRectangle.getId();

        Shape savedCircle = shapeRepository.save(new Shape(null, "MyCircle", "circle", null, 5.0, 5.0, 3.0));
        circleId = savedCircle.getId();

        Shape savedPolygon = shapeRepository.save(new Shape(null, "MyPolygon", "polygon", "1,1,2,2,3,3", null, null, null));
        polygonId = savedPolygon.getId();

        Shape savedOverlappingRect1 = shapeRepository.save(new Shape(null, "OverlappingRect1", "rectangle", "0,0,5,5", null, null, null));
        overlappingRect1Id = savedOverlappingRect1.getId();

        Shape savedOverlappingRect2 = shapeRepository.save(new Shape(null, "OverlappingRect2", "rectangle", "3,3,8,8", null, null, null));
        overlappingRect2Id = savedOverlappingRect2.getId();
    }

    @Test
    void testGetAllShapes() throws Exception {
        mockMvc.perform(get("/api/shapes")
                        .with(user("admin").roles("USER", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("MyRectangle"))
                .andExpect(jsonPath("$[1].type").value("circle"));
    }

    @Test
    void testGetShapeById() throws Exception {
        mockMvc.perform(get("/api/shapes/" + rectangleId)
                        .with(user("admin").roles("USER", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rectangleId))
                .andExpect(jsonPath("$.name").value("MyRectangle"))
                .andExpect(jsonPath("$.type").value("rectangle"));
    }

    @Test
    void testCreateRectangleShape() throws Exception {
        ShapeRequestDto newShapeDto = new ShapeRequestDto("NewRectangle", "rectangle", "0,0,20,20", null, null, null);

        mockMvc.perform(post("/api/shapes")
                        .with(user("admin").roles("USER", "ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newShapeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("NewRectangle"))
                .andExpect(jsonPath("$.type").value("rectangle"))
                .andExpect(jsonPath("$.coordinates").value("0,0,20,20"));

        assertEquals(6, shapeRepository.count());
        assertTrue(shapeRepository.findByName("NewRectangle").isPresent());
    }

    @Test
    void testUpdateShape() throws Exception {
        ShapeRequestDto updatedShapeDto = new ShapeRequestDto("UpdatedRectangle", "rectangle", "0,0,15,15", null, null, null);

        mockMvc.perform(put("/api/shapes/" + rectangleId)
                        .with(user("admin").roles("USER", "ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedShapeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rectangleId))
                .andExpect(jsonPath("$.name").value("UpdatedRectangle"))
                .andExpect(jsonPath("$.coordinates").value("0,0,15,15"));

        Shape updatedShapeInDb = shapeRepository.findById(rectangleId).orElseThrow();
        assertEquals("UpdatedRectangle", updatedShapeInDb.getName());
        assertEquals("0,0,15,15", updatedShapeInDb.getCoordinates());
    }

    @Test
    void testDeleteShape() throws Exception {
        mockMvc.perform(delete("/api/shapes/" + circleId)
                        .with(user("admin").roles("USER", "ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(4, shapeRepository.count());
        assertTrue(shapeRepository.findById(circleId).isEmpty());
    }

    @Test
    void testGetOverlappingShapeIds() throws Exception {
        mockMvc.perform(get("/api/shapes/overlaps")
                        .with(user("admin").roles("USER", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

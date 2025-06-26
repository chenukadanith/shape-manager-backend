package com.example.shapemanegement.controller;

import com.example.shapemanegement.dto.request.AuthDto;
import com.example.shapemanegement.dto.request.UserDto;
import com.example.shapemanegement.entity.User;
import com.example.shapemanegement.exception.UnauthorizedException;
import com.example.shapemanegement.service.UserService;
import com.example.shapemanegement.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private  UserService userService;
    public AuthController(UserService userService){
        this.userService=userService;
    }
    @PostMapping("/login")
    public ResponseEntity<?>  login(@RequestBody AuthDto authDto){
        try {
            UserDto userDto= userService.findByUsernameAndPassword(authDto.getUserName(), authDto.getPassword());
            String token= JwtUtil.generateToken(userDto.getUsername());
            Map<String, Object> userDetailsMap = new HashMap<>();

            userDetailsMap.put("username", userDto.getUsername()); // Use the authenticated username

            // 3. Create the main response map
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", token); // Use the generated token
            responseBody.put("userDetails", userDetailsMap);

            // Return the structured JSON response with HTTP 200 OK
            return ResponseEntity.ok(responseBody);

        }
        catch (UnauthorizedException e){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }


    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto){
        User user = userService.createUser(userDto);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id",user.getId());
        responseBody.put("username",user.getUsername());

        return  ResponseEntity.ok(responseBody);
    }



}

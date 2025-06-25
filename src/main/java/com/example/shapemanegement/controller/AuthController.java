package com.example.shapemanegement.controller;

import com.example.shapemanegement.dto.request.AuthDto;
import com.example.shapemanegement.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public String  login(@RequestBody AuthDto authDto){
        if(authDto.getUserName().equals("chenuka") && authDto.getPassword().equals("1234")){
            String token= JwtUtil.generateToken(authDto.getUserName());
            return token;
        }
        return "invalid username or password";
    }
}

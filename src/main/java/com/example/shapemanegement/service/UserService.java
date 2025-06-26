package com.example.shapemanegement.service;

import com.example.shapemanegement.dto.request.UserDto;
import com.example.shapemanegement.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {
    public User createUser(UserDto userDto);
    UserDto findByUsernameAndPassword(String username,String password);

}

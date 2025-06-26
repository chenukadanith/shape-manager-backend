package com.example.shapemanegement.service.impl;

import com.example.shapemanegement.dto.request.UserDto;
import com.example.shapemanegement.entity.User;
import com.example.shapemanegement.exception.UnauthorizedException;
import com.example.shapemanegement.repository.UserRepository;
import com.example.shapemanegement.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service

public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Override
    public User createUser(UserDto userDto) {
        User user= new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
         return userRepository.save(user);


    }

    @Override
    public UserDto findByUsernameAndPassword(String username, String password) {
        Optional<User> byUsernameAndPassword = userRepository.findByUsernameAndPassword(username, password);
        if(byUsernameAndPassword.isPresent()){
            User user = byUsernameAndPassword.get();
            UserDto userDto =new UserDto();

            userDto.setUsername(user.getUsername());
            userDto.setPassword(user.getPassword());
            return userDto;
        }
        throw new UnauthorizedException("Invalid Username and password");

    }
}

package com.uniai.services;

import com.uniai.dto.ResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.exception.InvalidTokenException;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;
import com.uniai.security.JwtUtil;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signUp(SignUpDto userDto) {
        User user = User.builder()
                .username(userDto.getUsername())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        userRepository.save(user);


        return jwtUtil.generateToken(user.getUsername());
    }

    public String signIn(SignInDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user == null || !passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    public List<ResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> ResponseDto.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build()).toList();
    }

    public ResponseDto getResponseDtoByToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new InvalidTokenException();

        return ResponseDto.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
    }
}

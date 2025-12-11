package com.uniai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

}

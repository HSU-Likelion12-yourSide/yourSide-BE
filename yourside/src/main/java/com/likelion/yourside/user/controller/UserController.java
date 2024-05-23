package com.likelion.yourside.user.controller;

import com.likelion.yourside.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/user/")
public class UserController {
    private final UserService userService;
}

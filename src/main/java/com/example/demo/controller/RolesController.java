package com.example.demo.controller;

import com.example.demo.entity.Roles;
import com.example.demo.repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolesController {
    @Autowired
    private RolesRepository rolesRepository;

    @GetMapping
    public List<Roles> getAllRoles() {
        return rolesRepository.findAll();
    }
}

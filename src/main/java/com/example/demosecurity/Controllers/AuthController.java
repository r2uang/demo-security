package com.example.demosecurity.Controllers;

import com.example.demosecurity.Security.jwt.JwtUtils;
import com.example.demosecurity.DTO.JwtResponse;
import com.example.demosecurity.DTO.LoginRequest;
import com.example.demosecurity.DTO.MessageResponse;
import com.example.demosecurity.DTO.SignupRequest;
import com.example.demosecurity.Entity.ERole;
import com.example.demosecurity.Entity.Role;
import com.example.demosecurity.Entity.Users;
import com.example.demosecurity.Repository.RoleRepository;
import com.example.demosecurity.Repository.UserRepository;

import com.example.demosecurity.Service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/signin")
    @ResponseBody
    public ResponseEntity<?> authenticateUser(@Validated @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> responseUser(@Validated @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: username is already taken"));
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: email is already taken"));
        }

        Users users = new Users(signupRequest.getUsername(),
                signupRequest.getEmail(),
                 encoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if(strRoles == null){
            Role userRole=roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(()->new RuntimeException("Error:Role is not found."));
            roles.add(userRole);
        }else{
            strRoles.forEach(role->{
                switch(role){
                    case"admin":
                        Role adminRole=roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(()->new RuntimeException("Error:Role is not found."));
                        roles.add(adminRole);
                        break;
                    case"mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            .orElseThrow(()->new RuntimeException("Error:Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole=roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(()->new RuntimeException("Error:Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        users.setRoles(roles);
        userRepository.save(users);
        return ResponseEntity.ok(new MessageResponse("Users registered successfully!"));
    }
}

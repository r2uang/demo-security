package com.example.demosecurity.Repository;

import com.example.demosecurity.Entity.ERole;
import com.example.demosecurity.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ERole name);
}

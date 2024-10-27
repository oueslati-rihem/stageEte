package com.authentifcation.projectpitwo.dao;


import com.authentifcation.projectpitwo.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends CrudRepository<Role, String> {

    Role findByroleName(String roleName);
}

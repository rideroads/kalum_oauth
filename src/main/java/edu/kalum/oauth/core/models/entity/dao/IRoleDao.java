package edu.kalum.oauth.core.models.entity.dao;

import edu.kalum.oauth.core.models.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRoleDao extends JpaRepository<Role,Long> {
}

package edu.kalum.oauth.core.models.entity.dao.services;

import edu.kalum.oauth.core.models.entity.Role;

import java.util.List;

public interface IRoleService {
    public List<Role> findAll();
    public Role findById(Long id);
    public Role save(Role role);
    public void deleteById(Long id);
    public void delete(Role role);
}

package edu.kalum.oauth.core.models.entity.dao.services;

import edu.kalum.oauth.core.models.entity.Role;
import edu.kalum.oauth.core.models.entity.dao.IRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceimpl implements IRoleService{

    @Autowired
    private IRoleDao roleDao;
    @Override
    public List<Role> findAll() {
        return roleDao.findAll();
    }

    @Override
    public Role findById(Long id) {
        return roleDao.findById(id).orElse(null);
    }

    @Override
    public Role save(Role role) {
        return roleDao.save(role);
    }

    @Override
    public void deleteById(Long id) {
        this.roleDao.deleteById(id);
    }

    @Override
    public void delete(Role role) {
        this.roleDao.delete(role);
    }
}

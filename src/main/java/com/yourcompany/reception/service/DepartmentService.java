package com.yourcompany.reception.service;

import com.yourcompany.reception.entity.Department;
import com.yourcompany.reception.entity.Institution;
import com.yourcompany.reception.mapper.DepartmentMapper;
import com.yourcompany.reception.mapper.InstitutionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private InstitutionMapper institutionMapper;

    public List<Department> getAllDepartments() {
        return departmentMapper.selectAll();
    }

    public List<Institution> getAllInstitutions() {
        return institutionMapper.selectAll();
    }

    public boolean saveOrUpdate(Department department) {
        if (department.getId() == null) {
            return departmentMapper.insert(department) > 0;
        } else {
            return departmentMapper.update(department) > 0;
        }
    }

    public boolean deleteDepartment(Integer id) {
        return departmentMapper.delete(id) > 0;
    }
}
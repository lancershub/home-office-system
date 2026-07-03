package com.yourcompany.reception.service;

import com.yourcompany.reception.entity.Institution;
import com.yourcompany.reception.mapper.InstitutionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionMapper institutionMapper;

    public List<Institution> getAllInstitutions() {
        return institutionMapper.selectAll();
    }

    public boolean saveOrUpdate(Institution inst) {
        if (inst.getId() == null) {
            return institutionMapper.insert(inst) > 0;
        } else {
            return institutionMapper.update(inst) > 0;
        }
    }

    public boolean deleteInstitution(Integer id) {
        return institutionMapper.delete(id) > 0;
    }
}
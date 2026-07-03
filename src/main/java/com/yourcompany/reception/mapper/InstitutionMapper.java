package com.yourcompany.reception.mapper;

import com.yourcompany.reception.entity.Institution;
import java.util.List;

public interface InstitutionMapper {
    List<Institution> selectAll();
    int insert(Institution institution);
    int update(Institution institution);
    int delete(Integer id);
}
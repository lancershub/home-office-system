package com.yourcompany.reception.mapper;

import com.yourcompany.reception.entity.Department;
import java.util.List;

public interface DepartmentMapper {
    // 获取所有部门列表
    List<Department> selectAll();

    // 新增部门
    int insert(Department department);

    // 更新部门信息
    int update(Department department);

    // 删除部门
    int delete(Integer id);
}
package com.yourcompany.reception.service;

import com.yourcompany.reception.mapper.VisitorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VisitorService {

    // 注入我们刚才写好的 Mapper
    @Autowired
    private VisitorMapper visitorMapper;

    /**
     * 获取所有访客/员工及他们的部门信息
     */
    public List<Map<String, Object>> getAllVisitorsWithDept() {
        return visitorMapper.selectAllVisitors();
    }

    /**
     * 更新员工所属部门
     * @return boolean 返回是否更新成功
     */
    public boolean updateEmployeeDept(Integer userId, Integer deptId) {
        // 如果影响的行数 > 0，说明更新成功
        return visitorMapper.updateDept(userId, deptId) > 0;
    }
}
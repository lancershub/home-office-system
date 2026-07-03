package com.yourcompany.reception.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 访客/员工数据访问层
 */
public interface VisitorMapper {

    /**
     * 查询所有访客/员工
     * 这里使用 Map 接收，方便我们直接进行多表联查获取部门名称，而不需要新建 DTO 类
     * @return 包含 id, visitor_name, dept_id, dept_name 的列表
     */
    List<Map<String, Object>> selectAllVisitors();

    /**
     * 更新员工所属部门
     * 使用 @Param 注解是因为该方法有多个参数，MyBatis 需要它来识别 XML 中的占位符
     * @param userId 员工ID
     * @param deptId 部门ID
     * @return 影响的行数
     */
    int updateDept(@Param("userId") Integer userId, @Param("deptId") Integer deptId);
}
package com.github.xionghui.microservice.business.dao.rule.project;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.github.xionghui.microservice.business.bean.BusinessConstants;
import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghui.microservice.business.bean.enums.BusinessHttpResultEnum;
import com.github.xionghui.microservice.business.bean.enums.ProjectTypeEnum;
import com.github.xionghui.microservice.business.feign.EurekaServiceUtils;
import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpResultEnum;
import com.github.xionghuicoder.microservice.common.dao.rule.IBatchBeforeRule;

public class CheckProjectParamsBatchInsertBeforeRule implements IBatchBeforeRule<ProjectDomain> {

  @Override
  public void beforeRule(List<ProjectDomain> beanList, List<ProjectDomain> originBeanList) {
    Set<String> employeeNameSet = this.queryEmployeeNames();
    for (ProjectDomain bean : beanList) {
      String name = bean.getName();
      String type = bean.getType();
      String employeeName = bean.getEmployeeName();
      Integer money = bean.getMoney();
      Timestamp time = bean.getTime();
      if (name == null || type == null || employeeName == null || money == null || money < 0
          || time == null) {
        throw new BusinessException("params is not full", HttpResultEnum.ParamsError);
      }

      boolean isNameMatch = name.matches(BusinessConstants.NAME_REGIX);
      if (!isNameMatch) {
        throw new BusinessException("name is illegal", BusinessHttpResultEnum.ProjectNameIllegal);
      }

      if (!ProjectTypeEnum.checkValue(type)) {
        throw new BusinessException(type + " is illegal", BusinessHttpResultEnum.ProjectTypeIllegal,
            type);
      }

      if (!employeeNameSet.contains(employeeName)) {
        throw new BusinessException(employeeName + " is illegal",
            BusinessHttpResultEnum.EmployeeNameIllegal, employeeName);
      }
    }
  }

  private Set<String> queryEmployeeNames() {
    JSONObject body = new JSONObject();
    @SuppressWarnings("unchecked")
    List<String> data =
        (List<String>) EurekaServiceUtils.getService("queryEmployeeNames", body.toString());
    Set<String> requireTypeSet = new HashSet<>();
    for (String employeeName : data) {
      requireTypeSet.add(employeeName);
    }
    return requireTypeSet;
  }
}

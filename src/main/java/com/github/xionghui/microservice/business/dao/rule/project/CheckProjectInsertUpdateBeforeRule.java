package com.github.xionghui.microservice.business.dao.rule.project;

import java.sql.Timestamp;

import com.alibaba.fastjson.JSONObject;
import com.github.xionghui.microservice.business.bean.BusinessConstants;
import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghui.microservice.business.bean.enums.BusinessHttpResultEnum;
import com.github.xionghui.microservice.business.bean.enums.ProjectTypeEnum;
import com.github.xionghui.microservice.business.feign.EurekaServiceUtils;
import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpResultEnum;
import com.github.xionghuicoder.microservice.common.dao.rule.IBeforeRule;

public class CheckProjectInsertUpdateBeforeRule implements IBeforeRule<ProjectDomain> {
  private final boolean isInsert;

  public CheckProjectInsertUpdateBeforeRule(boolean isInsert) {
    this.isInsert = isInsert;
  }

  @Override
  public void beforeRule(ProjectDomain bean, ProjectDomain originBean) {
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

    // update数据没变动的话，尽可能少调用接口
    if (this.isInsert || !employeeName.equals(originBean.getEmployeeName())) {
      this.checkEmployeeName(employeeName);
    }
  }

  private void checkEmployeeName(String employeeName) {
    JSONObject body = new JSONObject();
    body.put("employeeName", employeeName);
    Boolean data = (Boolean) EurekaServiceUtils.getService("checkEmployeeName", body.toString());
    if (data == null || !data) {
      throw new BusinessException(employeeName + " is illegal",
          BusinessHttpResultEnum.EmployeeNameIllegal, employeeName);
    }
  }
}

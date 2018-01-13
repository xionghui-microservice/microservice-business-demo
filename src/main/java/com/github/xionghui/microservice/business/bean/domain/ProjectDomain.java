package com.github.xionghui.microservice.business.bean.domain;

import java.sql.Timestamp;

import com.github.xionghuicoder.microservice.common.bean.CommonDomain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ProjectDomain extends CommonDomain {

  private String name;
  private String type;
  private String employeeName;
  private Integer money;
  private Timestamp time;

}

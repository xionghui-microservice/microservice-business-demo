package com.github.xionghui.microservice.business.bean.enums;

import com.github.xionghuicoder.microservice.common.bean.enums.IHttpResultEnum;

public enum BusinessHttpResultEnum implements IHttpResultEnum {
  ProjectNameIllegal(2000, "m00000"), // 项目名称格式不正确
  ProjectTypeIllegal(2002, "m00001"), // 项目类型{0}不存在
  EmployeeNameIllegal(2004, "m00002"), // 员工名称{0}不存在
  CallBaseServiceError(2006, "m00003"), // 调用基础数据{0}接口失败
  ExcelEmptyError(2008, "m00004"), // 导入的excel为空
  ExcelDataError(2010, "m00005"), // 解析excel数据失败
  ExcelHeaderIllegal(2012, "m00006"), // excel表头不正确,请下载最新excel
  ;

  public final int code;
  public final String languageCode;

  private BusinessHttpResultEnum(int code, String languageCode) {
    this.code = code;
    this.languageCode = languageCode;
  }

  @Override
  public int getCode() {
    return this.code;
  }

  @Override
  public String getLanguageCode() {
    return this.languageCode;
  }
}

package com.github.xionghui.microservice.business.service;

import com.github.xionghuicoder.microservice.common.bean.HttpResult;
import com.github.xionghuicoder.microservice.common.bean.ServiceParamsBean;
import com.github.xionghuicoder.microservice.common.bean.UploadServiceParamsBean;

public interface IProjectService {

  HttpResult<?> queryProjectType(ServiceParamsBean serviceParamsBean);

  HttpResult<?> queryEmployeeNames(ServiceParamsBean serviceParamsBean);

  void downloadProjectExcel(ServiceParamsBean serviceParamsBean);

  HttpResult<?> uploadProject(UploadServiceParamsBean uploadServiceParamsBean);

  void exportProject(ServiceParamsBean serviceParamsBean);

  HttpResult<?> insertProject(ServiceParamsBean serviceParamsBean);

  HttpResult<?> updateProject(ServiceParamsBean serviceParamsBean);

  HttpResult<?> deleteProject(ServiceParamsBean serviceParamsBean);

  HttpResult<?> batchDeleteProject(ServiceParamsBean serviceParamsBean);

  HttpResult<?> queryProject(ServiceParamsBean serviceParamsBean);
}

package com.github.xionghui.microservice.business.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghui.microservice.business.bean.enums.BusinessHttpResultEnum;
import com.github.xionghui.microservice.business.bean.enums.ProjectTypeEnum;
import com.github.xionghui.microservice.business.dao.IProjectDao;
import com.github.xionghui.microservice.business.feign.EurekaServiceUtils;
import com.github.xionghui.microservice.business.service.IProjectService;
import com.github.xionghui.microservice.business.utils.ExcelUtils;
import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.annotation.EnablePathConfigAnnotation;
import com.github.xionghuicoder.microservice.common.annotation.MenuAnnotation;
import com.github.xionghuicoder.microservice.common.annotation.PathConfigAnnotation;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.bean.HttpResult;
import com.github.xionghuicoder.microservice.common.bean.ServiceParamsBean;
import com.github.xionghuicoder.microservice.common.bean.UploadServiceParamsBean;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpRequestMethod;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpResultEnum;
import com.github.xionghuicoder.microservice.common.utils.BatchOperationParamsUtils;
import com.github.xionghuicoder.microservice.common.utils.CommonJsonUtils;
import com.github.xionghuicoder.microservice.common.utils.DownloadHttpHeaderUtil;

@Component
@EnablePathConfigAnnotation
@MenuAnnotation("business/project")
public class ProjectServiceImpl implements IProjectService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  @Autowired
  private ApplicationContext applicationContext;

  private static final String[] QUERY_KEY =
      new String[] {"uuid", "name", "type", "employeeName", "money", "time", "note"};

  @Override
  @PathConfigAnnotation(value = "queryProjectType", method = HttpRequestMethod.GET)
  public HttpResult<?> queryProjectType(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("queryProjectType begin");
    JSONArray data = ProjectTypeEnum.getArray();
    LOGGER.info("queryProjectType end");
    return HttpResult.custom(HttpResultEnum.Success).setData(data).build();
  }

  @Override
  @PathConfigAnnotation(value = "queryEmployeeNames", method = HttpRequestMethod.GET)
  public HttpResult<?> queryEmployeeNames(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("queryEmployeeNames begin");
    JSONObject body = new JSONObject();
    Object data = EurekaServiceUtils.getService("queryEmployeeNames", body.toString());
    LOGGER.info("queryEmployeeNames end");
    return HttpResult.custom(HttpResultEnum.Success).setData(data).build();
  }

  @Override
  @PathConfigAnnotation(value = "downloadProject", uri = "/download",
      method = HttpRequestMethod.GET)
  public void downloadProjectExcel(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("downloadProjectExcel begin");
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletResponse response = attributes.getResponse();
    DownloadHttpHeaderUtil.setFileDownloadHeader(attributes.getRequest(), response,
        "project-v1.xls");
    ExcelUtils.buildFinanceXls(response);
    LOGGER.info("downloadProjectExcel end");
  }

  @Override
  @PathConfigAnnotation(value = "uploadProject", uri = "/upload", method = HttpRequestMethod.POST)
  public HttpResult<?> uploadProject(UploadServiceParamsBean uploadServiceParamsBean) {
    LOGGER.info("uploadProject begin");
    MultipartFile[] files = uploadServiceParamsBean.getFiles();
    if (files.length != 1) {
      throw new BusinessException("files.length illegal: " + files.length,
          HttpResultEnum.ParamsError);
    }
    String fileName = files[0].getOriginalFilename();
    boolean isXls = fileName.endsWith(".xls");
    if (fileName == null || (!isXls && !fileName.endsWith(".xlsx"))) {
      throw new BusinessException("fileName illegal: " + fileName, HttpResultEnum.ParamsError);
    }
    List<ProjectDomain> domainList = ExcelUtils.parseProjectExcel(isXls, files[0]);
    if (domainList.size() == 0) {
      throw new BusinessException("excel is empty", BusinessHttpResultEnum.ExcelEmptyError);
    }
    String empId = uploadServiceParamsBean.getUser().getEmpId();
    for (ProjectDomain domain : domainList) {
      domain.setCreator(empId);
    }
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    iProjectDao.batchInsertProject(domainList);
    LOGGER.info("uploadProject end");
    return HttpResult.custom(HttpResultEnum.ImportSuccess).build();
  }

  @Override
  @PathConfigAnnotation(value = "exportProject", uri = "/download", method = HttpRequestMethod.GET)
  public void exportProject(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("exportProject begin");
    JSONArray uuids = serviceParamsBean.getBodyJson().getJSONArray("uuids");
    if (uuids == null || uuids.size() == 0) {
      throw new BusinessException("exportFinanceBudget uuids is null", HttpResultEnum.ParamsError);
    }
    List<ProjectDomain> domainList = this.buildExportData(uuids);
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletResponse response = attributes.getResponse();
    DownloadHttpHeaderUtil.setFileDownloadHeader(attributes.getRequest(), response, "project.xls");
    ExcelUtils.buildProjectXls(domainList, response);
    LOGGER.info("exportProject end");
  }

  private List<ProjectDomain> buildExportData(JSONArray uuids) {
    List<String> uuidList = new ArrayList<>();
    for (Object obj : uuids) {
      if (obj instanceof String) {
        uuidList.add((String) obj);
      }
    }
    List<ProjectDomain> domainList = null;
    if (uuidList.size() > 0) {
      IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
      domainList = iProjectDao.queryExportProject(uuidList);
    }
    return domainList;
  }

  @Override
  @PathConfigAnnotation(value = "insertProject", method = HttpRequestMethod.POST)
  public HttpResult<?> insertProject(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("insertProject begin");
    ProjectDomain bean = JSON.parseObject(serviceParamsBean.getBody(), ProjectDomain.class);
    String empId = serviceParamsBean.getUser().getEmpId();
    bean.setCreator(empId);
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    iProjectDao.insertProject(bean);
    JSONObject result = new JSONObject();
    result.put(CommonConstants.UUID, bean.getUuid());
    LOGGER.info("insertProject end");
    return HttpResult.custom(HttpResultEnum.InsertSuccess).setData(result).build();
  }

  @Override
  @PathConfigAnnotation(value = "updateProject", method = HttpRequestMethod.POST)
  public HttpResult<?> updateProject(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("updateProject begin");
    ProjectDomain bean = BatchOperationParamsUtils.dealUpdateParams(serviceParamsBean.getBodyJson(),
        ProjectDomain.class);
    String empId = serviceParamsBean.getUser().getEmpId();
    bean.setUpdater(empId);
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    iProjectDao.updateProject(bean);
    LOGGER.info("updateProject end");
    return HttpResult.custom(HttpResultEnum.UpdateSuccess).build();
  }

  @Override
  @PathConfigAnnotation(value = "deleteProject", method = HttpRequestMethod.POST)
  public HttpResult<?> deleteProject(ServiceParamsBean serviceParamsBean) {
    ProjectDomain bean = BatchOperationParamsUtils.dealDeleteParams(serviceParamsBean.getBodyJson(),
        ProjectDomain.class);
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    iProjectDao.deleteProject(bean);
    LOGGER.info("deleteProject end");
    return HttpResult.custom(HttpResultEnum.DeleteSuccess).build();
  }

  @Override
  @PathConfigAnnotation(value = "batchDeleteProject", method = HttpRequestMethod.POST)
  public HttpResult<?> batchDeleteProject(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("batchDeleteProject begin");
    List<ProjectDomain> beanList = BatchOperationParamsUtils
        .dealBatchDeleteParams(serviceParamsBean.getBodyJson(), ProjectDomain.class);
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    int count = iProjectDao.batchDeleteProject(beanList);
    LOGGER.info("batchDeleteProject end");
    return HttpResult.custom(HttpResultEnum.BatchDeleteSuccess).setArgs(String.valueOf(count))
        .build();
  }

  @Override
  @PathConfigAnnotation(value = "queryProject", method = HttpRequestMethod.GET)
  public HttpResult<?> queryProject(ServiceParamsBean serviceParamsBean) {
    LOGGER.info("queryProject begin");
    JSONObject bodyJson = serviceParamsBean.getBodyJson();
    Integer pageNum = bodyJson.getInteger("pageNum");
    Integer pageSize = bodyJson.getInteger("pageSize");
    if (pageNum == null || pageNum <= 0 || pageSize == null || pageSize <= 0) {
      throw new BusinessException("params is not full", HttpResultEnum.ParamsError);
    }
    String name = bodyJson.getString("name");
    IProjectDao iProjectDao = this.applicationContext.getBean(IProjectDao.class);
    Page<ProjectDomain> domainPage =
        (Page<ProjectDomain>) iProjectDao.queryProject(name, pageNum, pageSize);
    JSONObject result = new JSONObject();
    result.put("total", domainPage.getTotal());
    result.put("pages", domainPage.getPages());
    result.put("pageNum", domainPage.getPageNum());
    result.put("pageSize", domainPage.getPageSize());
    JSONArray data = new JSONArray();
    result.put("data", data);
    for (ProjectDomain domain : domainPage) {
      JSONObject json = CommonJsonUtils.object2Json(domain, QUERY_KEY);
      data.add(json);

      String type = json.getString("type");
      JSONObject budgetAreaJson = ProjectTypeEnum.getValue(type);
      json.put("type", budgetAreaJson);
    }
    LOGGER.info("queryProject end");
    return HttpResult.custom(HttpResultEnum.QuerySuccess).setData(result).build();
  }
}

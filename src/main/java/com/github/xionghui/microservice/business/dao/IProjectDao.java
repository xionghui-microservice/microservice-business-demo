package com.github.xionghui.microservice.business.dao;

import java.util.List;

import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;

public interface IProjectDao {

  void insertProject(ProjectDomain bean);

  int batchInsertProject(List<ProjectDomain> beanList);

  void updateProject(ProjectDomain bean);

  void deleteProject(ProjectDomain bean);

  int batchDeleteProject(List<ProjectDomain> beanList);

  List<ProjectDomain> queryProject(String name, int pageNum, int pageSize);

  List<ProjectDomain> queryExportProject(List<String> uuidList);
}

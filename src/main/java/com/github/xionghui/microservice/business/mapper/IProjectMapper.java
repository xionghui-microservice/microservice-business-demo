package com.github.xionghui.microservice.business.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghuicoder.microservice.common.dao.IBaseDao;

@Mapper
public interface IProjectMapper extends IBaseDao<ProjectDomain> {

  List<ProjectDomain> query(String name);

  List<ProjectDomain> queryExport(List<String> uuidList);
}

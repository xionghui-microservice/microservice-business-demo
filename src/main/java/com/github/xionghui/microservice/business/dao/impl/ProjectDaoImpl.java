package com.github.xionghui.microservice.business.dao.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.page.PageMethod;
import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghui.microservice.business.dao.IProjectDao;
import com.github.xionghui.microservice.business.dao.rule.project.CheckProjectInsertUpdateBeforeRule;
import com.github.xionghui.microservice.business.dao.rule.project.CheckProjectParamsBatchInsertBeforeRule;
import com.github.xionghui.microservice.business.mapper.IProjectMapper;
import com.github.xionghuicoder.microservice.common.dao.AbstractBaseDao;
import com.github.xionghuicoder.microservice.common.dao.rule.IBatchBeforeRule;
import com.github.xionghuicoder.microservice.common.dao.rule.IBeforeRule;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProjectDaoImpl extends AbstractBaseDao<ProjectDomain> implements IProjectDao {

  @Autowired
  private IProjectMapper iProjectMapper;

  @PostConstruct
  public void initIBaseDao() {
    this.iBaseDao = this.iProjectMapper;
  }

  @Override
  public void insertProject(ProjectDomain bean) {
    IBeforeRule<ProjectDomain> checkProjectParamsInsertUpdateBeforeRule =
        new CheckProjectInsertUpdateBeforeRule(true);
    this.addBeforeRule(checkProjectParamsInsertUpdateBeforeRule);

    this.insert(bean);
  }

  @Override
  public int batchInsertProject(List<ProjectDomain> beanList) {
    IBatchBeforeRule<ProjectDomain> checkProjectParamsBatchInsertBeforeRule =
        new CheckProjectParamsBatchInsertBeforeRule();
    this.addBatchBeforeRule(checkProjectParamsBatchInsertBeforeRule);

    return super.batchInsert(beanList);
  }

  @Override
  public void updateProject(ProjectDomain bean) {
    IBeforeRule<ProjectDomain> checkProjectParamsInsertUpdateBeforeRule =
        new CheckProjectInsertUpdateBeforeRule(false);
    this.addBeforeRule(checkProjectParamsInsertUpdateBeforeRule);

    this.update(bean);
  }

  @Override
  public void deleteProject(ProjectDomain bean) {
    super.delete(bean);
  }

  @Override
  public int batchDeleteProject(List<ProjectDomain> beanList) {
    return super.batchDelete(beanList);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProjectDomain> queryProject(String name, int pageNum, int pageSize) {
    PageMethod.startPage(pageNum, pageSize);
    if (name != null) {
      name = name.trim();
      if (name.length() == 0) {
        name = null;
      } else {
        name = "%" + name + "%";
      }
    }
    List<ProjectDomain> domainList = this.iProjectMapper.query(name);
    return domainList;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProjectDomain> queryExportProject(List<String> uuidList) {
    return this.iProjectMapper.queryExport(uuidList);
  }
}

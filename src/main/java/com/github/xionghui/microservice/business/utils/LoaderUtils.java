package com.github.xionghui.microservice.business.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.github.xionghuicoder.clearpool.core.IConnectionPool;
import com.github.xionghuicoder.microservice.common.BusinessException;

@Component
public class LoaderUtils {
  private static final String DATASOURCE = "dataSource";

  @Autowired
  private ApplicationContext applicationContext;

  @PostConstruct
  public void init() {
    Object dataSource = this.applicationContext.getBean(DATASOURCE);
    if (dataSource instanceof IConnectionPool) {
      IConnectionPool iConnectionPool = (IConnectionPool) dataSource;
      iConnectionPool.init();

      this.createTables(iConnectionPool);
    }
  }

  private void createTables(IConnectionPool iConnectionPool) {
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = iConnectionPool.getConnection();
      stmt = conn.createStatement();
      stmt.execute("create table `microservice_business_project` (\n"
          + "  `id` bigint(20) not null auto_increment comment '自增数字id',\n"
          + "  `uuid` varchar(64) not null comment '无规律字符串id',\n" + "  \n"
          + "  `name` varchar(32) not null comment '项目名称',\n"
          + "  `type` varchar(32) not null comment '项目类型',\n"
          + "  `employee_name` varchar(32) not null comment '项目员工',\n"
          + "  `money` int(11) not null comment '项目花费',\n"
          + "  `time` datetime not null comment '项目时间',\n" + "\n"
          + "  `note` longtext default null comment '备注',\n"
          + "  `version` int(11) not null default 1 comment '乐观锁',\n"
          + "  `ds` tinyint(1) not null default false comment '逻辑删除标志',\n"
          + "  `creator` varchar(128) not null comment '创建人',\n"
          + "  `create_time` datetime not null comment '创建时间',\n"
          + "  `updater` varchar(128) default null comment '修改人',\n"
          + "  `update_time` datetime default null comment '修改时间',\n" +
          // " `sys_timer` timestamp not null default current_timestamp on update current_timestamp
          // comment '操作时间',\n" +
          "  primary key (`id`)\n" + ") engine=innodb default charset=utf8");
    } catch (SQLException e) {
      throw new BusinessException(e);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // swallow it
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          // swallow it
        }
      }
    }
  }
}

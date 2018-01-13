package com.github.xionghui.microservice.business.feign;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.xionghui.microservice.business.bean.enums.BusinessHttpResultEnum;
import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.bean.HttpResult;

@Component
public class EurekaServiceUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EurekaServiceUtils.class);

  private static IBaseService IBASE_SERVICE;

  @Autowired
  private IBaseService iBaseService;

  @PostConstruct
  public void init() {
    IBASE_SERVICE = this.iBaseService;
  }

  public static Object getService(String function, String body) {
    LOGGER.info("EurekaServiceUtils getService begin, function: {}, body: {}", function, body);
    HttpResult<?> result = null;
    try {
      result = IBASE_SERVICE.getService(function, body);
    } catch (Exception e) {
      throw new BusinessException("iBaseService getService error: " + result,
          BusinessHttpResultEnum.CallBaseServiceError, function);
    }
    LOGGER.info("EurekaServiceUtils getService result: {}", result);
    if (result == null || result.getCode() != 0) {
      throw new BusinessException("iBaseService getService error: " + result,
          BusinessHttpResultEnum.CallBaseServiceError, function);
    }
    LOGGER.info("EurekaServiceUtils getService end");
    return result.getData();
  }
}

package com.github.xionghui.microservice.business.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.xionghuicoder.microservice.common.bean.HttpResult;

@FeignClient(name = "microservice-base-demo")
public interface IBaseService {

  @RequestMapping(method = RequestMethod.GET)
  HttpResult<?> getService(@RequestParam("function") String function,
      @RequestParam("body") String body);
}

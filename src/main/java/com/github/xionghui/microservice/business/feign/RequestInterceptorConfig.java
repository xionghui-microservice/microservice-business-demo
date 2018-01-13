package com.github.xionghui.microservice.business.feign;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.xionghuicoder.microservice.common.bean.CommonConstants;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * feign透传header <br>
 *
 * 需要设置hystrix.command.default.execution.isolation.strategy为SEMAPHORE，
 * 否则会使用线程池导致获取不到HttpServletRequest <br>
 *
 * note: 一定不能设置所有header，否则会导致返回数据编码错误；<br>
 * 比如有影响的header<tt>accept-encoding</tt>：gzip。
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class RequestInterceptorConfig {

  @Bean
  public RequestInterceptor headerInterceptor() {
    return new RequestInterceptor() {

      @Override
      public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
          while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (CommonConstants.LANGUAGE_COOKIE_HEADER.equals(name)
                || CommonConstants.PERMISSION_HEAD.equals(name)
                || CommonConstants.USER_HEAD.equals(name)) {
              String values = request.getHeader(name);
              requestTemplate.header(name, values);
            }
          }
        }
      }
    };
  }
}

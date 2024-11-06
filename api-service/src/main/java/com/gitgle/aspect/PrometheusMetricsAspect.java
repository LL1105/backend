package com.gitgle.aspect;


import io.micrometer.core.instrument.Metrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class PrometheusMetricsAspect {
 
    // 切入所有controller包下的请求方法
    @Pointcut("execution(* com.gitgle.controller..*.*(..))")
    public void controllerPointcut() {
    }
 
    @Around("controllerPointcut()")
    public Object MetricsCollector(ProceedingJoinPoint joinPoint) throws Throwable {
 
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = StringUtils.hasText( request.getParameter("userId"))?request.getParameter("userId"):"no userId";
        String appId = StringUtils.hasText( request.getParameter("appId"))?request.getParameter("appId"):"no appId";
 
        // 获取api url
        String api = request.getServletPath();
        // 获取请求方法
        String method = request.getMethod();
        long timeMillis = System.currentTimeMillis();
        LocalDate now = LocalDate.now();
        String[] tags = new String[10];
        tags[0]="api";
        tags[1] = api;
        tags[2]="method";
        tags[3]=method;
        tags[4]="day";
        tags[5]=now.toString();
        tags[6]="appId";
        tags[7]=appId;
        tags[8]="userId";
        tags[9]=userId;
        // 请求次数加1
        //自定义的指标名称：http_request_test_all，指标包含数据
        Metrics.counter("http_request_test_all",tags).increment();
        Object object;
        try {
            object = joinPoint.proceed();
        } catch (Exception e) {
            // 请求失败次数加1
            Metrics.counter("http_request_test_error",tags).increment();
            throw e;
        } finally {
            long f = System.currentTimeMillis();
            long l = f - timeMillis;
            //记录请求响应时间
            Metrics.timer("http_request_test_time", tags).record(l, TimeUnit.MILLISECONDS);
        }
        return object;
    }
}
package com.fintracker_backend.fintracker.aop;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.fintracker_backend.fintracker.service.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();

        log.debug("Entering method: {}", methodName);

        try {
            Object result = joinPoint.proceed();

            long timeTaken = System.currentTimeMillis() - start;

            log.info("Executed: {} | Time: {} ms", methodName, timeTaken);

            return result;

        } catch (Exception ex) {

            log.error("Exception in: {} | message={}", methodName, ex.getMessage(), ex);

            throw ex;
        }
    }
    @PostConstruct
public void init() {
    System.out.println("🔥 LoggingAspect initialized");
}
}
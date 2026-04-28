package com.fintracker_backend.fintracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication
@EnableCaching
@EnableRetry
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class FintrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintrackerApplication.class, args);
	}

}
 
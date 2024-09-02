package com.tobby.echonga.salesforce;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SalesforceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesforceApplication.class, args);
	}

	@EnableWebMvc
	public class MvcConfig implements WebMvcConfigurer {
		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {

			registry.addResourceHandler("/DataTables/**")
					.addResourceLocations("classpath:/static/DataTables/")
					.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
			registry.addResourceHandler("/src/**").addResourceLocations("classpath:/static/src/")
					.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
			registry.addResourceHandler("/vendors/**").addResourceLocations("classpath:/static/vendors/")
					.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
		}
	}
}

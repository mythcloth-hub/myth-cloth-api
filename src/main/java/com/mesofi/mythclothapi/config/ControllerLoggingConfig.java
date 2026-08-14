package com.mesofi.mythclothapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ControllerLoggingConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ControllerMethodLoggingInterceptor());
    }

    private static class ControllerMethodLoggingInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if (!(handler instanceof HandlerMethod handlerMethod)) {
                return true;
            }

            Class<?> controllerClass = handlerMethod.getBeanType();
            boolean isController = AnnotatedElementUtils.hasAnnotation(controllerClass, RestController.class)
                    || AnnotatedElementUtils.hasAnnotation(controllerClass, Controller.class);
            if (!isController) {
                return true;
            }

            ControllerLoggingConfig.log.info("Executing: {}#{} [{} {}]", controllerClass.getSimpleName(),
                    handlerMethod.getMethod().getName(), request.getMethod(), request.getRequestURI());
            return true;
        }
    }
}

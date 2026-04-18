package com.mesofi.mythclothapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/** Test-only configuration that enables Bean Validation for method parameters and return values. */
@TestConfiguration
public class MethodValidationTestConfig {

  /**
   * Creates the validator used by method-level validation in tests.
   *
   * @return validator factory bean for test contexts
   */
  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  /**
   * Registers the post processor that applies validation to method invocations.
   *
   * @return method validation post processor wired to the test validator
   */
  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor() {
    MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
    processor.setValidator(validator());
    return processor;
  }
}

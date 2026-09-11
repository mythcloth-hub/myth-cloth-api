package com.mesofi.mythclothapi.figurines;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.config.MethodValidationTestConfig;

@ActiveProfiles("test")
@SpringBootTest(classes = {FigurineService.class, MethodValidationTestConfig.class})
public class FigurineServiceTest {

}
package com.mesofi.mythclothapi.security.roles;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.config.MapperTestConfig;

@ActiveProfiles("test")
@SpringBootTest(classes = {RoleService.class, MapperTestConfig.class})
public class RoleServiceTest {

}

package com.mesofi.mythclothapi.figurines;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(FigurineController.class)
public class FigurineControllerTest {

  @MockitoBean FigurineService service;

  @Test
  void createFigurine_shouldReturn400_whenBodyIsMissing() throws Exception {}
}

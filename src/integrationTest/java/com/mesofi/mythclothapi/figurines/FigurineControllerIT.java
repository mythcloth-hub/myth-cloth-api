package com.mesofi.mythclothapi.figurines;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mesofi.mythclothapi.it.CatalogSelector;
import com.mesofi.mythclothapi.it.FigurineScenario;
import com.mesofi.mythclothapi.it.FigurineScenarioContext;
import com.mesofi.mythclothapi.it.FigurineScenarioExtension;
import com.mesofi.mythclothapi.it.ScenarioRequest;

@ExtendWith(FigurineScenarioExtension.class)
public class FigurineControllerIT {

  @Test
  @FigurineScenario(
      name = "Create prototype figurine",
      payloads = {
        @ScenarioRequest(
            resource = "prototype_figurine_create.json",
            catalog =
                @CatalogSelector(
                    lineUp = "Myth Cloth EX",
                    series = "Saintia Sho",
                    group = "Gold Saint")),
        @ScenarioRequest(
            resource = "prototype_figurine_create.json",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE)
      })
  void createPrototypeFigurine_returnsCreated(FigurineScenarioContext ctx) {
    System.out.println("Executing scenario: AAA");
  }
}

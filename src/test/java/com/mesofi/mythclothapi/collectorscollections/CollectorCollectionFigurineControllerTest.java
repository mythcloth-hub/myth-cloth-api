package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineControllerTest {

  @Mock private CollectorCollectionFigurineService service;

  @Test
  void addFigurineToCollection_shouldDelegateToService_whenInvokedDirectly() {
    CollectorCollectionFigurineController controller =
        new CollectorCollectionFigurineController(service);

    var response = controller.addFigurineToCollection(null, 5L, 9L);

    assertThat(response).isNull();
    verify(service).addFigurineToCollection(5L, 9L);
  }
}

package com.mesofi.mythclothapi.anniversaries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;

@SpringBootTest(
    classes = {AnniversaryService.class, MapperTestConfig.class, MethodValidationTestConfig.class})
public class AnniversaryServiceTest {

  @Autowired private AnniversaryService anniversaryService;

  @MockitoBean private AnniversaryRepository anniversaryRepository;

  @Test
  void createAnniversary_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    AnniversaryReq request = request("Saint Seiya 20th Anniversary", 20);

    when(anniversaryRepository.save(any(Anniversary.class)))
        .thenAnswer(
            invocation -> {
              Anniversary entity = invocation.getArgument(0);
              entity.setId(1L);
              return entity;
            });

    // Act
    AnniversaryResp response = anniversaryService.createAnniversary(request);

    // Assert
    assertThat(response).isEqualTo(new AnniversaryResp(1L, "Saint Seiya 20th Anniversary", 20));

    ArgumentCaptor<Anniversary> captor = ArgumentCaptor.forClass(Anniversary.class);
    verify(anniversaryRepository).save(captor.capture());

    Anniversary saved = captor.getValue();
    assertThat(saved.getDescription()).isEqualTo("Saint Seiya 20th Anniversary");
    assertThat(saved.getYear()).isEqualTo(20);
    assertThat(saved.getFigurines().size()).isEqualTo(0);
  }

  @Test
  void retrieveAnniversary_shouldReturnMappedResponse_whenAnniversaryExists() {
    // Arrange
    Anniversary anniversary = anniversary(7L, "Hades Chapter Celebration", 15);
    when(anniversaryRepository.findById(7L)).thenReturn(Optional.of(anniversary));

    // Act
    AnniversaryResp response = anniversaryService.retrieveAnniversary(7L);

    // Assert
    assertThat(response).isEqualTo(new AnniversaryResp(7L, "Hades Chapter Celebration", 15));
    verify(anniversaryRepository).findById(7L);
  }

  @Test
  void retrieveAnniversary_shouldThrowNotFoundException_whenAnniversaryDoesNotExist() {
    // Arrange
    when(anniversaryRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> anniversaryService.retrieveAnniversary(99L))
        .isInstanceOfSatisfying(
            AnniversaryNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Anniversary not found");
              assertThat(ex.getId()).isEqualTo(99L);
            });

    verify(anniversaryRepository).findById(99L);
  }

  @Test
  void retrieveAnniversaries_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    when(anniversaryRepository.findAll())
        .thenReturn(
            List.of(
                anniversary(1L, "Classic Anime Anniversary", 10),
                anniversary(2L, "Manga Legacy Anniversary", 30)));

    // Act
    List<AnniversaryResp> responses = anniversaryService.retrieveAnniversaries();

    // Assert
    assertThat(responses)
        .containsExactly(
            new AnniversaryResp(1L, "Classic Anime Anniversary", 10),
            new AnniversaryResp(2L, "Manga Legacy Anniversary", 30));

    verify(anniversaryRepository).findAll();
  }

  @Test
  void updateAnniversary_shouldUpdateExistingEntityAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    Anniversary existing = anniversary(3L, "Old Description", 5);
    AnniversaryReq request = request("Updated Anniversary", 25);

    when(anniversaryRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(anniversaryRepository.save(any(Anniversary.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    AnniversaryResp response = anniversaryService.updateAnniversary(3L, request);

    // Assert
    assertThat(response).isEqualTo(new AnniversaryResp(3L, "Updated Anniversary", 25));

    ArgumentCaptor<Anniversary> captor = ArgumentCaptor.forClass(Anniversary.class);
    verify(anniversaryRepository).save(captor.capture());

    Anniversary saved = captor.getValue();
    assertThat(saved).isSameAs(existing);
    assertThat(saved.getDescription()).isEqualTo("Updated Anniversary");
    assertThat(saved.getYear()).isEqualTo(25);

    verify(anniversaryRepository).findById(3L);
  }

  @Test
  void updateAnniversary_shouldThrowNotFoundException_whenAnniversaryDoesNotExist() {
    // Arrange
    AnniversaryReq request = request("Updated Anniversary", 25);
    when(anniversaryRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> anniversaryService.updateAnniversary(77L, request))
        .isInstanceOfSatisfying(
            AnniversaryNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Anniversary not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(anniversaryRepository).findById(77L);
    verify(anniversaryRepository, never()).save(any(Anniversary.class));
  }

  @Test
  void removeAnniversary_shouldDeleteById_whenAnniversaryExists() {
    // Arrange
    when(anniversaryRepository.existsById(5L)).thenReturn(true);

    // Act
    anniversaryService.removeAnniversary(5L);

    // Assert
    verify(anniversaryRepository).existsById(5L);
    verify(anniversaryRepository).deleteById(5L);
  }

  @Test
  void removeAnniversary_shouldThrowNotFoundException_whenAnniversaryDoesNotExist() {
    // Arrange
    when(anniversaryRepository.existsById(8L)).thenReturn(false);

    // Act + Assert
    assertThatThrownBy(() -> anniversaryService.removeAnniversary(8L))
        .isInstanceOfSatisfying(
            AnniversaryNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Anniversary not found");
              assertThat(ex.getId()).isEqualTo(8L);
            });

    verify(anniversaryRepository).existsById(8L);
    verify(anniversaryRepository, never()).deleteById(8L);
  }

  private AnniversaryReq request(String description, int year) {
    return new AnniversaryReq(description, year);
  }

  private Anniversary anniversary(Long id, String description, Integer year) {
    Anniversary anniversary = new Anniversary();
    anniversary.setId(id);
    anniversary.setDescription(description);
    anniversary.setYear(year);
    return anniversary;
  }
}

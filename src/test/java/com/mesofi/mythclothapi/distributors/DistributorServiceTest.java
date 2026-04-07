package com.mesofi.mythclothapi.distributors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;

@SpringBootTest(
    classes = {DistributorService.class, MapperTestConfig.class, MethodValidationTestConfig.class})
class DistributorServiceTest {

  @Autowired private DistributorService distributorService;

  @MockitoBean private DistributorRepository distributorRepository;

  @Test
  void createDistributor_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    DistributorReq request =
        new DistributorReq(DistributorName.DAM, CountryCode.MX, "https://app.tamashii.mx/");

    Distributor saved = createEntity(1L, DistributorName.DAM, CountryCode.MX, request.website());

    when(distributorRepository.existsByNameAndCountry(DistributorName.DAM, CountryCode.MX))
        .thenReturn(false);
    when(distributorRepository.save(org.mockito.ArgumentMatchers.any(Distributor.class)))
        .thenReturn(saved);

    // Act
    DistributorResp response = distributorService.createDistributor(request);

    // Assert
    assertThat(response)
        .isNotNull()
        .extracting(
            DistributorResp::id,
            DistributorResp::name,
            DistributorResp::description,
            DistributorResp::countryCode,
            DistributorResp::website)
        .containsExactly(
            1L,
            DistributorName.DAM.toString(),
            DistributorName.DAM.getDescription(),
            CountryCode.MX.toString(),
            "https://app.tamashii.mx/");

    ArgumentCaptor<Distributor> distributorCaptor = ArgumentCaptor.forClass(Distributor.class);
    verify(distributorRepository).save(distributorCaptor.capture());

    Distributor persisted = distributorCaptor.getValue();
    assertThat(persisted.getId()).isNull();
    assertThat(persisted.getName()).isEqualTo(DistributorName.DAM);
    assertThat(persisted.getCountry()).isEqualTo(CountryCode.MX);
    assertThat(persisted.getWebsite()).isEqualTo("https://app.tamashii.mx/");

    verify(distributorRepository).existsByNameAndCountry(DistributorName.DAM, CountryCode.MX);
  }

  @Test
  void createDistributor_shouldThrowAlreadyExistsException_whenDuplicateNameAndCountry() {
    // Arrange
    DistributorReq request =
        new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "https://tamashiiweb.com/");

    when(distributorRepository.existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP))
        .thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.createDistributor(request))
        .isInstanceOf(DistributorAlreadyExistsException.class)
        .hasMessage("Distributor already exists")
        .extracting(ex -> ((DistributorAlreadyExistsException) ex).getCauseDetail())
        .isEqualTo("Distributor already exists: BANDAI - JP");

    verify(distributorRepository).existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP);
    verify(distributorRepository, never())
        .save(org.mockito.ArgumentMatchers.any(Distributor.class));
  }

  @Test
  void retrieveDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    when(distributorRepository.findById(0L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> distributorService.retrieveDistributor(0L))
        .isInstanceOf(DistributorNotFoundException.class)
        .hasMessage("Distributor not found")
        .extracting(ex -> ((DistributorNotFoundException) ex).getId())
        .isEqualTo(0L);

    verify(distributorRepository).findById(0L);
  }

  @Test
  void retrieveDistributor_shouldReturnMappedResponse_whenDistributorExists() {
    // Arrange
    Distributor entity = createEntity(5L, DistributorName.BLUE_FIN, CountryCode.US, null);
    when(distributorRepository.findById(5L)).thenReturn(Optional.of(entity));

    // Act
    DistributorResp response = distributorService.retrieveDistributor(5L);

    // Assert
    assertThat(response)
        .isNotNull()
        .extracting(
            DistributorResp::id,
            DistributorResp::name,
            DistributorResp::description,
            DistributorResp::countryCode,
            DistributorResp::website)
        .containsExactly(
            5L,
            DistributorName.BLUE_FIN.toString(),
            DistributorName.BLUE_FIN.getDescription(),
            CountryCode.US.toString(),
            null);

    verify(distributorRepository).findById(5L);
  }

  @Test
  void retrieveDistributors_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    Distributor first = createEntity(1L, DistributorName.BANDAI, CountryCode.JP, null);
    Distributor second =
        createEntity(2L, DistributorName.DS_DISTRIBUTIONS, CountryCode.ES, "https://sdd.com");

    when(distributorRepository.findAll()).thenReturn(List.of(first, second));

    // Act
    List<DistributorResp> responses = distributorService.retrieveDistributors();

    // Assert
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0))
        .extracting(DistributorResp::id, DistributorResp::name, DistributorResp::countryCode)
        .containsExactly(1L, "BANDAI", "JP");
    assertThat(responses.get(1))
        .extracting(
            DistributorResp::id,
            DistributorResp::name,
            DistributorResp::description,
            DistributorResp::countryCode,
            DistributorResp::website)
        .containsExactly(2L, "DS_DISTRIBUTIONS", "DS Distribuciones", "ES", "https://sdd.com");

    verify(distributorRepository).findAll();
  }

  @Test
  void retrieveDistributors_shouldReturnEmptyList_whenNoDistributorsExist() {
    // Arrange
    when(distributorRepository.findAll()).thenReturn(List.of());

    // Act
    List<DistributorResp> responses = distributorService.retrieveDistributors();

    // Assert
    assertThat(responses).isNotNull().isEmpty();
    verify(distributorRepository).findAll();
  }

  @Test
  void updateDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    DistributorReq request = new DistributorReq(DistributorName.DTM, CountryCode.MX, null);
    when(distributorRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> distributorService.updateDistributor(77L, request))
        .isInstanceOf(DistributorNotFoundException.class)
        .hasMessage("Distributor not found")
        .extracting(ex -> ((DistributorNotFoundException) ex).getId())
        .isEqualTo(77L);

    verify(distributorRepository).findById(77L);
    verify(distributorRepository, never())
        .save(org.mockito.ArgumentMatchers.any(Distributor.class));
  }

  @Test
  void updateDistributor_shouldThrowAlreadyExistsException_whenNewNameAndCountryConflict() {
    // Arrange
    DistributorReq request =
        new DistributorReq(DistributorName.DAM, CountryCode.MX, "https://new.example.com");
    Distributor existing = createEntity(4L, DistributorName.BANDAI, CountryCode.JP, null);

    when(distributorRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(distributorRepository.existsByNameAndCountry(DistributorName.DAM, CountryCode.MX))
        .thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.updateDistributor(4L, request))
        .isInstanceOf(DistributorAlreadyExistsException.class)
        .hasMessage("Distributor already exists")
        .extracting(ex -> ((DistributorAlreadyExistsException) ex).getCauseDetail())
        .isEqualTo("Distributor already exists: DAM - MX");

    verify(distributorRepository).findById(4L);
    verify(distributorRepository).existsByNameAndCountry(DistributorName.DAM, CountryCode.MX);
    verify(distributorRepository, never())
        .save(org.mockito.ArgumentMatchers.any(Distributor.class));
  }

  @Test
  void updateDistributor_shouldUpdateAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    DistributorReq request =
        new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "https://bandai.com");
    Distributor existing = createEntity(3L, DistributorName.DTM, CountryCode.MX, "https://old.com");

    when(distributorRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(distributorRepository.existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP))
        .thenReturn(false);
    when(distributorRepository.save(existing)).thenReturn(existing);

    // Act
    DistributorResp response = distributorService.updateDistributor(3L, request);

    // Assert
    assertThat(response)
        .isNotNull()
        .extracting(
            DistributorResp::id,
            DistributorResp::name,
            DistributorResp::description,
            DistributorResp::countryCode,
            DistributorResp::website)
        .containsExactly(
            3L,
            DistributorName.BANDAI.toString(),
            DistributorName.BANDAI.getDescription(),
            CountryCode.JP.toString(),
            "https://bandai.com");

    verify(distributorRepository).findById(3L);
    verify(distributorRepository).existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP);
    verify(distributorRepository).save(existing);

    assertThat(existing.getName()).isEqualTo(DistributorName.BANDAI);
    assertThat(existing.getCountry()).isEqualTo(CountryCode.JP);
    assertThat(existing.getWebsite()).isEqualTo("https://bandai.com");
  }

  @Test
  void removeDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    when(distributorRepository.existsById(8L)).thenReturn(false);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.removeDistributor(8L))
        .isInstanceOf(DistributorNotFoundException.class)
        .hasMessage("Distributor not found")
        .extracting(ex -> ((DistributorNotFoundException) ex).getId())
        .isEqualTo(8L);

    verify(distributorRepository).existsById(8L);
    verify(distributorRepository, never()).deleteById(8L);
  }

  @Test
  void removeDistributor_shouldDeleteById_whenDistributorExists() {
    // Arrange
    when(distributorRepository.existsById(5L)).thenReturn(true);

    // Act
    distributorService.removeDistributor(5L);

    // Assert
    verify(distributorRepository).existsById(5L);
    verify(distributorRepository).deleteById(5L);
  }

  private Distributor createEntity(
      Long id, DistributorName name, CountryCode country, String website) {
    Distributor distributor = new Distributor();
    distributor.setId(id);
    distributor.setName(name);
    distributor.setCountry(country);
    distributor.setWebsite(website);
    return distributor;
  }
}

package com.mesofi.mythclothapi.distributors;

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
public class DistributorServiceTest {

  @Autowired private DistributorService distributorService;

  @MockitoBean private DistributorRepository distributorRepository;

  @Test
  void createDistributor_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    DistributorReq request = request(DistributorName.BANDAI, CountryCode.JP, null);

    when(distributorRepository.existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP))
        .thenReturn(false);
    when(distributorRepository.save(any(Distributor.class)))
        .thenAnswer(
            invocation -> {
              Distributor entity = invocation.getArgument(0);
              entity.setId(1L);
              return entity;
            });

    // Act
    DistributorResp response = distributorService.createDistributor(request);

    // Assert
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.name()).isEqualTo(DistributorName.BANDAI.toString());
    assertThat(response.description()).isEqualTo(DistributorName.BANDAI.getDescription());
    assertThat(response.countryCode()).isEqualTo(CountryCode.JP.toString());

    ArgumentCaptor<Distributor> captor = ArgumentCaptor.forClass(Distributor.class);
    verify(distributorRepository).save(captor.capture());

    Distributor saved = captor.getValue();
    assertThat(saved.getName()).isEqualTo(DistributorName.BANDAI);
    assertThat(saved.getCountry()).isEqualTo(CountryCode.JP);
  }

  @Test
  void createDistributor_shouldThrowAlreadyExistsException_whenDuplicateNameAndCountry() {
    // Arrange
    DistributorReq request = request(DistributorName.DAM, CountryCode.MX, null);

    when(distributorRepository.existsByNameAndCountry(DistributorName.DAM, CountryCode.MX))
        .thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.createDistributor(request))
        .isInstanceOfSatisfying(
            DistributorAlreadyExistsException.class,
            ex -> {
              assertThat(ex.getName()).isEqualTo(DistributorName.DAM.toString());
              assertThat(ex.getCountry()).isEqualTo(CountryCode.MX.toString());
            });

    verify(distributorRepository, never()).save(any(Distributor.class));
  }

  @Test
  void retrieveDistributor_shouldReturnMappedResponse_whenDistributorExists() {
    // Arrange
    Distributor entity = distributor(5L, DistributorName.BLUE_FIN, CountryCode.US, null);
    when(distributorRepository.findById(5L)).thenReturn(Optional.of(entity));

    // Act
    DistributorResp response = distributorService.retrieveDistributor(5L);

    // Assert
    assertThat(response.id()).isEqualTo(5L);
    assertThat(response.name()).isEqualTo(DistributorName.BLUE_FIN.toString());
    assertThat(response.countryCode()).isEqualTo(CountryCode.US.toString());
    verify(distributorRepository).findById(5L);
  }

  @Test
  void retrieveDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    when(distributorRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> distributorService.retrieveDistributor(99L))
        .isInstanceOfSatisfying(
            DistributorNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Distributor not found");
              assertThat(ex.getId()).isEqualTo(99L);
            });

    verify(distributorRepository).findById(99L);
  }

  @Test
  void retrieveDistributors_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    when(distributorRepository.findAll())
        .thenReturn(
            List.of(
                distributor(1L, DistributorName.BANDAI, CountryCode.JP, null),
                distributor(2L, DistributorName.DAM, CountryCode.MX, "https://dam.com")));

    // Act
    List<DistributorResp> responses = distributorService.retrieveDistributors();

    // Assert
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).id()).isEqualTo(1L);
    assertThat(responses.get(0).name()).isEqualTo(DistributorName.BANDAI.toString());
    assertThat(responses.get(1).id()).isEqualTo(2L);
    assertThat(responses.get(1).website()).isEqualTo("https://dam.com");

    verify(distributorRepository).findAll();
  }

  @Test
  void retrieveDistributors_shouldReturnEmptyList_whenNoDistributorsExist() {
    // Arrange
    when(distributorRepository.findAll()).thenReturn(List.of());

    // Act
    List<DistributorResp> responses = distributorService.retrieveDistributors();

    // Assert
    assertThat(responses).isEmpty();
    verify(distributorRepository).findAll();
  }

  @Test
  void updateDistributor_shouldUpdateAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    Distributor existing = distributor(3L, DistributorName.BANDAI, CountryCode.JP, null);
    DistributorReq request = request(DistributorName.BANDAI, CountryCode.JP, "https://bandai.com");

    when(distributorRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(distributorRepository.existsByNameAndCountry(DistributorName.BANDAI, CountryCode.JP))
        .thenReturn(true);
    when(distributorRepository.save(any(Distributor.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    DistributorResp response = distributorService.updateDistributor(3L, request);

    // Assert
    assertThat(response.id()).isEqualTo(3L);
    assertThat(response.website()).isEqualTo("https://bandai.com");

    ArgumentCaptor<Distributor> captor = ArgumentCaptor.forClass(Distributor.class);
    verify(distributorRepository).save(captor.capture());
    assertThat(captor.getValue()).isSameAs(existing);

    verify(distributorRepository).findById(3L);
  }

  @Test
  void updateDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    DistributorReq request = request(DistributorName.DTM, CountryCode.MX, null);
    when(distributorRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> distributorService.updateDistributor(77L, request))
        .isInstanceOfSatisfying(
            DistributorNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Distributor not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(distributorRepository).findById(77L);
    verify(distributorRepository, never()).save(any(Distributor.class));
  }

  @Test
  void updateDistributor_shouldThrowAlreadyExistsException_whenNewNameAndCountryConflict() {
    // Arrange
    Distributor existing = distributor(4L, DistributorName.BANDAI, CountryCode.JP, null);
    DistributorReq request = request(DistributorName.DAM, CountryCode.MX, null);

    when(distributorRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(distributorRepository.existsByNameAndCountry(DistributorName.DAM, CountryCode.MX))
        .thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.updateDistributor(4L, request))
        .isInstanceOfSatisfying(
            DistributorAlreadyExistsException.class,
            ex -> {
              assertThat(ex.getName()).isEqualTo(DistributorName.DAM.toString());
              assertThat(ex.getCountry()).isEqualTo(CountryCode.MX.toString());
            });

    verify(distributorRepository, never()).save(any(Distributor.class));
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

  @Test
  void removeDistributor_shouldThrowNotFoundException_whenDistributorDoesNotExist() {
    // Arrange
    when(distributorRepository.existsById(8L)).thenReturn(false);

    // Act + Assert
    assertThatThrownBy(() -> distributorService.removeDistributor(8L))
        .isInstanceOfSatisfying(
            DistributorNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Distributor not found");
              assertThat(ex.getId()).isEqualTo(8L);
            });

    verify(distributorRepository).existsById(8L);
    verify(distributorRepository, never()).deleteById(8L);
  }

  private DistributorReq request(DistributorName name, CountryCode country, String website) {
    return new DistributorReq(name, country, website);
  }

  private Distributor distributor(
      Long id, DistributorName name, CountryCode country, String website) {
    Distributor d = new Distributor();
    d.setId(id);
    d.setName(name);
    d.setCountry(country);
    d.setWebsite(website);
    return d;
  }
}

package com.mesofi.mythclothapi.distributors;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DTM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.Distributor;

@ExtendWith(MockitoExtension.class)
public class DistributorServiceTest {

  @Mock private DistributorRepository repository;

  @Mock private DistributorMapper mapper;

  @InjectMocks private DistributorService service;

  private DistributorReq request;
  private Distributor entity;
  private DistributorResp response;

  @BeforeEach
  void setup() {
    request = new DistributorReq(BANDAI, JP, "https://tamashiiweb.com/");
    entity = new Distributor();
    entity.setId(1L);
    entity.setName(BANDAI);
    entity.setCountry(JP);
    entity.setWebsite("https://tamashiiweb.com/");

    response =
        new DistributorResp(1L, "BANDAI", "Tamashii Nations", "JP", "https://tamashiiweb.com/");
  }

  @Test
  void createDistributor_shouldThrowException_whenDistributorExists() {
    // Arrange
    when(mapper.toDistributor(request)).thenReturn(entity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> service.createDistributor(request))
        .isInstanceOf(DistributorAlreadyExistsException.class);

    verify(mapper).toDistributor(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
  }

  @Test
  void createDistributor_shouldCreateSuccessfully_whenUnique() {
    // Arrange
    when(mapper.toDistributor(request)).thenReturn(entity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(false);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDistributorResp(entity)).thenReturn(response);

    // Act
    DistributorResp result = service.createDistributor(request);

    // Assert
    assertThat(result).isEqualTo(response);
    verify(mapper).toDistributor(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
    verify(repository).save(entity);
    verify(mapper).toDistributorResp(entity);
  }

  @Test
  void retrieveDistributor_shouldThrowException_whenNotFound() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> service.retrieveDistributor(1L))
        .isInstanceOf(DistributorNotFoundException.class);

    verify(repository).findById(1L);
  }

  @Test
  void retrieveDistributor_shouldReturnResponse_whenFound() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    when(mapper.toDistributorResp(entity)).thenReturn(response);

    // Act
    DistributorResp result = service.retrieveDistributor(1L);

    // Assert
    assertThat(result).isEqualTo(response);

    verify(repository).findById(1L);
    verify(mapper).toDistributorResp(entity);
  }

  @Test
  void retrieveDistributors_shouldReturnList() {
    // Arrange
    when(repository.findAll()).thenReturn(List.of(entity));
    when(mapper.toDistributorResp(entity)).thenReturn(response);

    // Act
    List<DistributorResp> result = service.retrieveDistributors();

    // Assert
    assertThat(result).containsExactly(response);

    verify(repository).findAll();
    verify(mapper).toDistributorResp(entity);
  }

  @Test
  void updateDistributor_shouldThrowException_whenDistributorNotFound() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> service.updateDistributor(1L, request))
        .isInstanceOf(DistributorNotFoundException.class);

    verify(repository).findById(1L);
  }

  @Test
  void updateDistributor_shouldThrowException_whenNewNameCountryExists() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.of(entity));

    Distributor incoming = new Distributor();
    incoming.setName(BANDAI);
    incoming.setCountry(JP);

    when(mapper.toDistributor(request)).thenReturn(incoming);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(true);

    // name+country mismatch triggers exception
    entity.setName(DTM);

    // Act + Assert
    assertThatThrownBy(() -> service.updateDistributor(1L, request))
        .isInstanceOf(DistributorAlreadyExistsException.class);

    verify(repository).findById(1L);
    verify(mapper).toDistributor(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
  }

  @Test
  void updateDistributor_shouldUpdateSuccessfully_whenValid() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.of(entity));

    Distributor mappedEntity = new Distributor();
    mappedEntity.setName(BANDAI);
    mappedEntity.setCountry(JP);

    when(mapper.toDistributor(request)).thenReturn(mappedEntity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(false);

    doAnswer(inv -> null).when(mapper).updateDistributor(request, entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDistributorResp(entity)).thenReturn(response);

    // Act
    DistributorResp result = service.updateDistributor(1L, request);

    // Assert
    assertThat(result).isEqualTo(response);

    verify(repository).findById(1L);
    verify(mapper).toDistributor(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
    verify(repository).save(entity);
    verify(mapper).toDistributorResp(entity);
  }

  @Test
  void removeDistributor_shouldThrowException_whenNotFound() {
    // Arrange
    when(repository.existsById(1L)).thenReturn(false);

    // Act + Assert
    assertThatThrownBy(() -> service.removeDistributor(1L))
        .isInstanceOf(DistributorNotFoundException.class);

    verify(repository).existsById(1L);
  }

  @Test
  void removeDistributor_shouldDeleteSuccessfully_whenExists() {
    // Arrange
    when(repository.existsById(1L)).thenReturn(true);

    // Act
    service.removeDistributor(1L);

    // Assert
    verify(repository).deleteById(1L);
  }
}

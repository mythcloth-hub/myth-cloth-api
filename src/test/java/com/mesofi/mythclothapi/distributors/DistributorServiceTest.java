package com.mesofi.mythclothapi.distributors;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DTM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DistributorServiceTest {

  @Mock private DistributorRepository repository;

  @Mock private DistributorMapper mapper;

  @InjectMocks private DistributorService service;

  private DistributorRequest request;
  private DistributorEntity entity;
  private DistributorResponse response;

  @BeforeEach
  void setup() {
    request = new DistributorRequest("BANDAI", "JP", "https://tamashiiweb.com/");
    entity = new DistributorEntity();
    entity.setId(1L);
    entity.setName(BANDAI);
    entity.setCountry(JP);
    entity.setWebsite("https://tamashiiweb.com/");

    response = new DistributorResponse(1L, "BANDAI", "JP", "https://tamashiiweb.com/");
  }

  @Test
  void createDistributor_shouldThrowException_whenDistributorExists() {
    // Arrange
    when(mapper.toDistributorEntity(request)).thenReturn(entity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(true);

    // Act + Assert
    assertThatThrownBy(() -> service.createDistributor(request))
        .isInstanceOf(DistributorAlreadyExistsException.class);

    verify(mapper).toDistributorEntity(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
  }

  @Test
  void createDistributor_shouldCreateSuccessfully_whenUnique() {
    // Arrange
    when(mapper.toDistributorEntity(request)).thenReturn(entity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(false);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDistributorResponse(entity)).thenReturn(response);

    // Act
    DistributorResponse result = service.createDistributor(request);

    // Assert
    assertThat(result).isEqualTo(response);
    verify(mapper).toDistributorEntity(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
    verify(repository).save(entity);
    verify(mapper).toDistributorResponse(entity);
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
    when(mapper.toDistributorResponse(entity)).thenReturn(response);

    // Act
    DistributorResponse result = service.retrieveDistributor(1L);

    // Assert
    assertThat(result).isEqualTo(response);

    verify(repository).findById(1L);
    verify(mapper).toDistributorResponse(entity);
  }

  @Test
  void retrieveDistributors_shouldReturnList() {
    // Arrange
    when(repository.findAll()).thenReturn(List.of(entity));
    when(mapper.toDistributorResponse(entity)).thenReturn(response);

    // Act
    List<DistributorResponse> result = service.retrieveDistributors();

    // Assert
    assertThat(result).containsExactly(response);

    verify(repository).findAll();
    verify(mapper).toDistributorResponse(entity);
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

    DistributorEntity incoming = new DistributorEntity();
    incoming.setName(BANDAI);
    incoming.setCountry(JP);

    when(mapper.toDistributorEntity(request)).thenReturn(incoming);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(true);

    // name+country mismatch triggers exception
    entity.setName(DTM);

    // Act + Assert
    assertThatThrownBy(() -> service.updateDistributor(1L, request))
        .isInstanceOf(DistributorAlreadyExistsException.class);

    verify(repository).findById(1L);
    verify(mapper).toDistributorEntity(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
  }

  @Test
  void updateDistributor_shouldUpdateSuccessfully_whenValid() {
    // Arrange
    when(repository.findById(1L)).thenReturn(Optional.of(entity));

    DistributorEntity mappedEntity = new DistributorEntity();
    mappedEntity.setName(BANDAI);
    mappedEntity.setCountry(JP);

    when(mapper.toDistributorEntity(request)).thenReturn(mappedEntity);
    when(repository.existsByNameAndCountry(BANDAI, JP)).thenReturn(false);

    doAnswer(inv -> null).when(mapper).updateDistributorEntity(request, entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDistributorResponse(entity)).thenReturn(response);

    // Act
    DistributorResponse result = service.updateDistributor(1L, request);

    // Assert
    assertThat(result).isEqualTo(response);

    verify(repository).findById(1L);
    verify(mapper).toDistributorEntity(request);
    verify(repository).existsByNameAndCountry(BANDAI, JP);
    verify(repository).save(entity);
    verify(mapper).toDistributorResponse(entity);
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

package com.mesofi.mythclothapi.distributors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest // Bootstraps only JPA components + H2
@ActiveProfiles("test")
public class DistributorRepositoryTest {
  @Autowired DistributorRepository repository;

  @Test
  void shouldThrowException_whenCountryAndNameAreNull() {
    // Arrange
    DistributorEntity distributor = createDistributor(null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(distributor))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"COUNTRY\""); // depends on DB dialect
  }

  @Test
  void shouldThrowException_whenNameIsNull() {
    // Arrange
    DistributorEntity distributor = createDistributor(null, CountryCode.MX, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(distributor))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"NAME\""); // depends on DB dialect
  }

  @Test
  void shouldCreateDistributor_whenValidDataProvided() {
    // Arrange
    DistributorEntity distributor =
        createDistributor(DistributorName.DAM, CountryCode.MX, "https://animexico-online.com/");

    // Act
    DistributorEntity saved = repository.save(distributor);

    // Assert
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo(DistributorName.DAM);
    assertThat(saved.getCountry()).isEqualTo(CountryCode.MX);
  }

  @Test
  void shouldFindDistributorById_whenExists() {
    // Arrange
    DistributorEntity distributor = createDistributor(DistributorName.DTM, CountryCode.MX, null);
    DistributorEntity saved = repository.save(distributor);

    // Act
    DistributorEntity found = repository.findById(saved.getId()).orElse(null);

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo(DistributorName.DTM);
  }

  @Test
  void shouldReturnTrue_whenDistributorExistsByNameAndCountry() {
    // Arrange
    repository.save(
        createDistributor(DistributorName.DAM, CountryCode.MX, "https://app.tamashii.mx/"));

    // Act
    boolean exists = repository.existsByNameAndCountry(DistributorName.DAM, CountryCode.MX);

    // Assert
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnFalse_whenDistributorDoesNotExistByNameAndCountry() {
    // Act
    boolean exists = repository.existsByNameAndCountry(DistributorName.BLUE_FIN, CountryCode.US);

    // Assert
    assertThat(exists).isFalse();
  }

  @Test
  void shouldUpdateDistributor_whenValidChangesProvided() {
    // Arrange
    DistributorEntity distributor =
        createDistributor(DistributorName.BLUE_FIN, CountryCode.US, "https://www.bluefincorp.com");
    DistributorEntity saved = repository.save(distributor);

    // Act
    saved.setWebsite("https://wholesale.bandai.com/");
    DistributorEntity updated = repository.save(saved);

    // Assert
    assertThat(updated.getWebsite()).isEqualTo("https://wholesale.bandai.com/");
  }

  @Test
  void shouldDeleteDistributor_whenValidIdProvided() {
    // Arrange
    DistributorEntity distributor =
        createDistributor(
            DistributorName.DS_DISTRIBUTIONS, CountryCode.ES, "https://www.sddistribuciones.com/");
    DistributorEntity saved = repository.save(distributor);

    // Act
    repository.delete(saved);

    // Assert
    assertThat(repository.findById(saved.getId())).isEmpty();
  }

  @Test
  void shouldThrowException_whenNameAndCountryAreDuplicated() {
    // Arrange
    DistributorEntity d1 = createDistributor(DistributorName.DTM, CountryCode.MX, "url1");
    DistributorEntity d2 = createDistributor(DistributorName.DTM, CountryCode.MX, "url2");

    repository.saveAndFlush(d1);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(d2))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "Unique index or primary key violation: \"PUBLIC.UK_DISTRIBUTOR_NAME_COUNTRY_INDEX_2");
  }

  private DistributorEntity createDistributor(
      DistributorName name, CountryCode country, String website) {

    DistributorEntity e = new DistributorEntity();
    e.setName(name);
    e.setCountry(country);
    e.setWebsite(website);
    return e;
  }
}

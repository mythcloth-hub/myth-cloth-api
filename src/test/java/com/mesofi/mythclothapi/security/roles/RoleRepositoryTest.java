package com.mesofi.mythclothapi.security.roles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.security.roles.model.Role;

@DataJpaTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldThrowException_whenCompletedAtIsNull() {
        // Arrange
        Role role = new Role();

        // Act + Assert
        assertThatThrownBy(() -> roleRepository.saveAndFlush(role)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistFigurineImport_whenRequiredFieldsArePresent() {
        // Arrange
        Role role = new Role();
        role.setName("Admin");
        role.setCreationDate(Instant.now());
        role.setUpdateDate(Instant.now());

        // Act
        Role saved = roleRepository.saveAndFlush(role);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Admin");
        assertThat(saved.getCreationDate()).isNotNull();
        assertThat(saved.getUpdateDate()).isNotNull();
    }
}

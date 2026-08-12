package com.mesofi.mythclothapi.figurineimports;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.figurineimports.service.FigurineImportService;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

@Import(SecurityConfig.class)
@WebMvcTest(value = FigurineImportController.class)
public class FigurineImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FigurineImportService figurineImportService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void importAllFigurinesFromPublicDrive_shouldReturn202AndTriggerImport() throws Exception {
        // Act
        mockMvc.perform(post("/figurines/load").with(jwt().authorities(new SimpleGrantedAuthority("figurines:load"))))
                .andExpect(status().isAccepted());

        // Assert
        verify(figurineImportService).importAllFigurinesFromPublicDrive();
    }

    @Test
    void importAllFigurinesFromPublicDrive_shouldReturn500AndTriggerImport() throws Exception {
        // Arrange
        doThrow(new FigurineImportException()).when(figurineImportService).importAllFigurinesFromPublicDrive();

        // Act & Assert
        mockMvc.perform(post("/figurines/load").with(jwt().authorities(new SimpleGrantedAuthority("figurines:load"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("There was an error importing figurines."))
                .andExpect(jsonPath("$.instance").value("/figurines/load")).andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.title").value("Figurine Import Error"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value("FIGURINE_IMPORT_ERROR"));
    }

    @Test
    void getFigurineImports_shouldReturn200AndListImports() throws Exception {
        // Arrange
        when(figurineImportService.getAllFigurineImports())
                .thenReturn(List.of(new FigurineImportResp(1L, 12, null, Instant.parse("2024-06-01T12:00:00Z"))));

        // Act & Assert
        mockMvc.perform(get("/figurines/imports").with(jwt().authorities(new SimpleGrantedAuthority("figurines:load"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].imported").value(12))
                .andExpect(jsonPath("$[0].completedAt").value("2024-06-01T12:00:00Z"))
                .andExpect(jsonPath("$[0].errorMessage").doesNotExist());
    }
}

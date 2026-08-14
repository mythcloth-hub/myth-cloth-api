package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineSummaryResp;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(value = FigurineController.class, properties = {"myth-cloth.security.cors-url=http://localhost:5173",
        "myth-cloth.security.jwt.secret=test-secret-test-secret-test-secret-1234",
        "myth-cloth.security.jwt.issuer=myth-cloth-api", "myth-cloth.security.jwt.ttl-minutes=60"})
@Import(SecurityConfig.class)
class FigurineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FigurineService service;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFigurine_shouldReturn404_whenPostingToRootPath() throws Exception {

        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("figurines:create")))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The URL you are calling does not exist."))
                .andExpect(jsonPath("$.instance").value("/")).andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.title").value("Endpoint not found")).andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createFigurine_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

        mockMvc.perform(post("/figurines").with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("figurines:create"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.figurines.dto.FigurineResp> com.mesofi.mythclothapi.figurines.FigurineController.createFigurine(com.mesofi.mythclothapi.figurines.dto.FigurineReq)"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Invalid body")).andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createFigurine_shouldReturn415_whenContentTypeIsMissing() throws Exception {

        mockMvc.perform(post("/figurines").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("figurines:create"))).content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("415"))
                .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createFigurine_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Validation Failed")).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.name").value("must not be blank"))
                .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
                .andExpect(jsonPath("$.errors.seriesId").value("must not be null"));
    }

    @Test
    void createFigurine_shouldReturn400_whenRequestBodyHasOnlyPartialFields() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Seiya\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Validation Failed")).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
                .andExpect(jsonPath("$.errors.seriesId").value("must not be null"));
    }

    @Test
    void createFigurine_shouldReturn400_whenDistributorsPayloadIsIncomplete() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Seiya\", \"distributors\":[{}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Validation Failed")).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
                .andExpect(jsonPath("$.errors.seriesId").value("must not be null"));
    }

    @Test
    void createFigurine_shouldReturn400_whenDistributorEntryLacksRequiredFields() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Seiya\", \"distributors\":[{}],\"lineUpId\":\"3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Validation Failed")).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.seriesId").value("must not be null"));
    }

    @Test
    void createFigurine_shouldReturn400_whenRequestBodyMissingGroupId() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Seiya\", \"distributors\":[{}],\"lineUpId\":\"3\",\"seriesId\":\"2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Validation Failed")).andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createFigurine_shouldReturn400_whenDistributorCurrencyHasUnknownValue() throws Exception {

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"name\":\"Seiya\", \"distributors\":[{\"currency\":\"=\"}],\"lineUpId\":\"3\",\"seriesId\":\"2\", \"groupId\":\"5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode` from String \"=\": not one of the values accepted for Enum class: [EUR, MXN, CAD, CNY, JPY, USD]"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Invalid body")).andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createFigurine_shouldReturn201AndLocationHeader() throws Exception {
        FigurineReq request = createFigurineRequest();
        FigurineResp response = createFigurineResponse(1L, "Pegasus Seiya");

        when(service.createFigurine(any())).thenReturn(response);

        mockMvc.perform(post("/figurines")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Pegasus Seiya"));

        verify(service).createFigurine(any());
    }

    @Test
    void retrieveFigurine_shouldReturn200_whenFigurineExists() throws Exception {
        FigurineResp response = createFigurineResponse(1L, "Pegasus Seiya");

        when(service.readFigurine(1L)).thenReturn(response);

        mockMvc.perform(get("/figurines/{id}", 1L)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Pegasus Seiya"));

        verify(service).readFigurine(1L);
    }

    @Test
    void retrieveFigurines_shouldReturnPaginatedPayload_whenPageAndSizeAreProvided() throws Exception {
        FigurineResp first = createFigurineResponse(1L, "Pegasus Seiya");
        FigurineResp second = createFigurineResponse(2L, "Dragon Shiryu");
        PageRequest pageRequest = PageRequest.of(0, 2);

        when(service.filterFigurines(any(FigurineFilter.class), eq(0), eq(2)))
                .thenReturn(new CollectablePageImpl<>(List.of(first, second), pageRequest, 5, 0));

        mockMvc.perform(get("/figurines").param("page", "0").param("size", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5)).andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(service).filterFigurines(any(FigurineFilter.class), eq(0), eq(2));
    }

    @Test
    void retrieveFigurines_shouldReturn500_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/figurines").param("page", "-1").param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        jsonPath("$.detail").value("retrieveFigurineDetails.page: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.instance").value("/figurines")).andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.title").value("Unexpected error occurred, try again later."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void retrieveFigurines_shouldReturnFilteredResults_whenNameIsProvided() throws Exception {
        FigurineResp first = createFigurineResponse(1L, "Pegasus Seiya");
        PageRequest pageRequest = PageRequest.of(0, 2);

        when(service.filterFigurines(any(FigurineFilter.class), eq(0), eq(2)))
                .thenReturn(new CollectablePageImpl<>(List.of(first), pageRequest, 1, 0));

        mockMvc.perform(get("/figurines").param("name", "seiya").param("page", "0").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Pegasus Seiya"));

        verify(service).filterFigurines(any(FigurineFilter.class), eq(0), eq(2));
    }

    @Test
    void retrieveFigurines_shouldReturnAll_whenNameIsShortOrMissing() throws Exception {
        FigurineResp first = createFigurineResponse(1L, "Pegasus Seiya");
        PageRequest pageRequest = PageRequest.of(0, 2);

        when(service.filterFigurines(any(FigurineFilter.class), eq(0), eq(2)))
                .thenReturn(new CollectablePageImpl<>(List.of(first), pageRequest, 1, 0));

        // name param too short
        mockMvc.perform(get("/figurines").param("name", "ab").param("page", "0").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));

        // name param missing
        mockMvc.perform(get("/figurines").param("page", "0").param("size", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(service, org.mockito.Mockito.times(2)).filterFigurines(any(FigurineFilter.class), eq(0), eq(2));
    }

    @Test
    void updateFigurine_shouldReturn200_whenRequestIsValid() throws Exception {
        FigurineReq request = createFigurineRequest();
        FigurineResp response = createFigurineResponse(1L, "Dragon Shiryu");

        when(service.updateFigurine(1L, request)).thenReturn(response);

        mockMvc.perform(put("/figurines/{id}", 1L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:update")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dragon Shiryu"));

        verify(service).updateFigurine(1L, request);
    }

    @Test
    void deleteFigurine_shouldReturn204_whenFigurineExists() throws Exception {
        mockMvc.perform(delete("/figurines/{id}", 1L).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("figurines:delete")))).andExpect(status().isNoContent());

        verify(service).deleteFigurine(1L);
    }

    @Test
    void retrieveFigurines_shouldSearchByName_whenNameIsExactlyThreeChars() throws Exception {
        FigurineResp first = createFigurineResponse(1L, "Abc");
        PageRequest pageRequest = PageRequest.of(0, 2);

        when(service.filterFigurines(any(FigurineFilter.class), eq(0), eq(2)))
                .thenReturn(new CollectablePageImpl<>(List.of(first), pageRequest, 1, 0));

        mockMvc.perform(get("/figurines").param("name", "abc").param("page", "0").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Abc"));

        verify(service).filterFigurines(any(FigurineFilter.class), eq(0), eq(2));
    }

    @Test
    void retrieveFigurines_shouldUseCollectorSelection_whenJwtIsAuthenticated() throws Exception {
        FigurineResp first = createFigurineResponse(1L, "Pegasus Seiya");
        PageRequest pageRequest = PageRequest.of(0, 2);
        when(service.retrieveCollectedFigurineIds(1L, 99L)).thenReturn(List.of(10L, 11L));
        when(service.filterFigurines(any(FigurineFilter.class), eq(0), eq(2)))
                .thenReturn(new CollectablePageImpl<>(List.of(first), pageRequest, 1, 2));

        mockMvc.perform(get("/figurines").with(jwt().jwt(jwt -> jwt.subject("1"))).param("collectionId", "99")
                .param("page", "0").param("size", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(service).retrieveCollectedFigurineIds(1L, 99L);
        verify(service).filterFigurines(any(FigurineFilter.class), eq(0), eq(2));
    }

    @Test
    void retrieveSelectableFigurines_shouldReturnIds_whenFiltersAreProvided() throws Exception {
        when(service.retrieveSelectableFigurines(any(FigurineFilter.class))).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get("/figurines/selectable-ids").param("name", "seiya")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1L)).andExpect(jsonPath("$[1]").value(2L));

        verify(service).retrieveSelectableFigurines(any(FigurineFilter.class));
    }

    @Test
    void retrieveFigurineSummaries_shouldReturnSummaries() throws Exception {
        FigurineSummaryResp summary = new FigurineSummaryResp(1L, "Pegasus Seiya",
                new com.mesofi.mythclothapi.catalogs.dto.CatalogResp(2L, "Myth Cloth EX"),
                "https://images.example/pegasus.jpg");
        when(service.retrieveFigurineSummaries(any(FigurineFilter.class))).thenReturn(List.of(summary));

        mockMvc.perform(get("/figurines/summary")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].displayableName").value("Pegasus Seiya"))
                .andExpect(jsonPath("$[0].lineUp.description").value("Myth Cloth EX"))
                .andExpect(jsonPath("$[0].officialImageUrl").value("https://images.example/pegasus.jpg"));

        verify(service).retrieveFigurineSummaries(any(FigurineFilter.class));
    }

    private FigurineReq createFigurineRequest() {
        return new FigurineReq("Pegasus Seiya", List.of(new DistributorReq(1L, JPY, 16000d, null, null, null, null)),
                "https://tamashiiweb.com/item/12345", 2L, 1L, 1L, 1L, null, true, false, false, false, false, false,
                false, false, false, true, "Bronze Saint", List.of("https://images.example/pegasus.jpg"),
                List.of("https://images.example/pegasus-fan.jpg"));
    }

    private FigurineResp createFigurineResponse(long id, String name) {
        return new FigurineResp(id, name, name + " Myth Cloth EX", List.of(), "https://tamashiiweb.com/item/12345",
                com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED,
                new com.mesofi.mythclothapi.catalogs.dto.CatalogResp(2L, "Tamashii Nations"),
                new com.mesofi.mythclothapi.catalogs.dto.CatalogResp(1L, "Myth Cloth EX"),
                new com.mesofi.mythclothapi.catalogs.dto.CatalogResp(1L, "Saint Seiya"),
                new com.mesofi.mythclothapi.catalogs.dto.CatalogResp(1L, "Bronze Saint"), null, true, false, false,
                false, false, false, false, false, false, true, "Bronze Saint",
                List.of("https://images.example/pegasus.jpg"), List.of("https://images.example/pegasus-fan.jpg"),
                List.of(), List.of(), null, null);
    }
}

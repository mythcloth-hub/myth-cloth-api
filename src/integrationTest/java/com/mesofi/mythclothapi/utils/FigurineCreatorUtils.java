package com.mesofi.mythclothapi.utils;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.common.CurrencyCode;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

/**
 * Utility class for creating figurines and related resources in integration
 * tests.
 *
 * <p>
 * This class provides methods to create basic figurine resources, including
 * lineups, series, groups, distributors, and figurines. It also includes
 * methods to remove resources after tests.
 *
 * <p>
 * The methods use a {@link RestClient} to perform HTTP requests to the API
 * endpoints.
 */
public final class FigurineCreatorUtils {

    public static final String LINE_UP = "/catalogs/lineups";
    public static final String LINE_UP_DEL = LINE_UP + "/{id}";

    public static final String SERIES = "/catalogs/series";
    public static final String SERIES_DEL = SERIES + "/{id}";

    public static final String GROUPS = "/catalogs/groups";
    public static final String GROUPS_DEL = GROUPS + "/{id}";

    public static final String DISTRIBUTORS = "/distributors";
    public static final String DISTRIBUTORS_DEL = DISTRIBUTORS + "/{id}";

    public static final String FIGURINES = "/figurines";
    public static final String FIGURINES_DEL = FIGURINES + "/{id}";

    /** Prevents instantiation. */
    private FigurineCreatorUtils() {
    }

    /**
     * Creates a basic figurine with associated resources (lineup, series, group,
     * distributor) using the provided {@link RestClient}.
     *
     * @param rest
     *            the {@link RestClient} to use for HTTP requests
     * @return a {@link FigurineIdentifiers} object containing the IDs of the
     *         created resources
     */
    public static FigurineIdentifiers createBasicFigurine(RestClient rest) {
        Long lineUpId = createCatalog(rest, LINE_UP, "Myth Cloth");
        Long seriesId = createCatalog(rest, SERIES, "Saint Seiya");
        Long groupId = createCatalog(rest, GROUPS, "Bronze Saint V2");
        Long distributorId = createDistributor(rest);

        com.mesofi.mythclothapi.figurines.dto.DistributorReq distributorReq = new com.mesofi.mythclothapi.figurines.dto.DistributorReq(
                distributorId, CurrencyCode.JPY, 13000d, null, null, null, null);

        FigurineReq figurineReq = createBasicFigurine(distributorReq, lineUpId, seriesId, groupId);
        Long figurineId = createFigurine(rest, figurineReq);

        return new FigurineIdentifiers(figurineId, distributorId, groupId, lineUpId, seriesId);
    }

    /**
     * Creates a catalog entry with the specified description using the provided
     * {@link RestClient}.
     *
     * @param rest
     *            the {@link RestClient} to use for HTTP requests
     * @param catalogUrl
     *            the URL of the catalog endpoint
     * @param description
     *            the description of the catalog entry
     * @return the ID of the created catalog entry
     */
    private static Long createCatalog(RestClient rest, String catalogUrl, String description) {
        CatalogReq catalogReq = new CatalogReq(description);
        ResponseEntity<CatalogResp> response = rest.post().uri(catalogUrl).body(catalogReq).retrieve()
                .toEntity(CatalogResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody().id();
    }

    /**
     * Creates a distributor entry using the provided {@link RestClient}.
     *
     * @param rest
     *            the {@link RestClient} to use for HTTP requests
     * @return the ID of the created distributor entry
     */
    private static Long createDistributor(RestClient rest) {
        DistributorReq distributorReq = new DistributorReq(BANDAI, JP, "https://tamashiiweb.com/");

        ResponseEntity<DistributorResp> response = rest.post().uri(DISTRIBUTORS).body(distributorReq).retrieve()
                .toEntity(DistributorResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody().id();
    }

    /**
     * Creates a figurine entry using the provided {@link RestClient} and
     * {@link FigurineReq}.
     *
     * @param rest
     *            the {@link RestClient} to use for HTTP requests
     * @param request
     *            the {@link FigurineReq} containing the figurine details
     * @return the ID of the created figurine entry
     */
    private static Long createFigurine(RestClient rest, FigurineReq request) {
        ResponseEntity<FigurineResp> response = rest.post().uri(FIGURINES).body(request).retrieve()
                .toEntity(FigurineResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody().id();
    }

    /**
     * Creates a basic figurine request with the specified distributor, lineup,
     * series, and group IDs.
     *
     * @param distributorReq
     *            the distributor request containing distributor details
     * @param lineUpId
     *            the ID of the lineup
     * @param seriesId
     *            the ID of the series
     * @param groupId
     *            the ID of the group
     * @return a {@link FigurineReq} object representing the figurine request
     */
    private static FigurineReq createBasicFigurine(com.mesofi.mythclothapi.figurines.dto.DistributorReq distributorReq,
            Long lineUpId, Long seriesId, Long groupId) {
        return new FigurineReq("Pegasus Seiya", List.of(distributorReq), null, null, lineUpId, seriesId, groupId, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Removes a resource using the provided {@link RestClient} and URI.
     *
     * @param rest
     *            the {@link RestClient} to use for HTTP requests
     * @param uri
     *            the URI of the resource to remove
     * @param uriVariables
     *            optional URI variables for the request
     */
    public static void removeResource(RestClient rest, String uri, @Nullable Object... uriVariables) {
        rest.delete().uri(uri, uriVariables).retrieve().toBodilessEntity();
    }
}

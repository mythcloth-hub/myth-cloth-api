package com.mesofi.mythclothapi.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.anniversaries.AnniversaryNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogRepositoryNotFoundException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.figurineevents.FigurineEventNotFoundException;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageAlreadyExistsException;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageNotFoundException;
import com.mesofi.mythclothapi.figurineimports.FigurineImportException;
import com.mesofi.mythclothapi.integration.ServiceName;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionAlreadyExistsException;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleAlreadyExistsException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.exceptions.RolePermissionAlreadyExistsException;
import com.mesofi.mythclothapi.stores.StoreNotFoundException;

class ExceptionMetadataTest {

    @Test
    void anniversaryNotFoundException_shouldExposeApiMetadata() {
        AnniversaryNotFoundException exception = new AnniversaryNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Anniversary not found",
                ErrorCode.FIGURINE_ANNIVERSARY_NOT_FOUND);
    }

    @Test
    void catalogNotFoundException_shouldExposeApiMetadata() {
        CatalogNotFoundException exception = new CatalogNotFoundException("groups");

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Catalog not found", ErrorCode.CATALOG_NOT_FOUND);
    }

    @Test
    void catalogRepositoryNotFoundException_shouldExposeApiMetadata() {
        CatalogRepositoryNotFoundException exception = new CatalogRepositoryNotFoundException("groups");

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Catalog repository not found",
                ErrorCode.CATALOG_REPOSITORY_NOT_FOUND);
    }

    @Test
    void collectorInvalidTokenException_shouldExposeApiMetadata() {
        CollectorInvalidTokenException exception = new CollectorInvalidTokenException("token expired");

        assertApiExceptionMetadata(exception, HttpStatus.UNAUTHORIZED, "Invalid token", ErrorCode.INVALID_TOKEN);
    }

    @Test
    void collectorNotFoundException_shouldExposeApiMetadata() {
        CollectorNotFoundException exception = new CollectorNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Collector not found",
                ErrorCode.COLLECTOR_NOT_FOUND);
    }

    @Test
    void collectorCollectionAlreadyExistsException_shouldExposeApiMetadata() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertApiExceptionMetadata(exception, HttpStatus.CONFLICT, "Collector collection already exists",
                ErrorCode.COLLECTOR_COLLECTION_ALREADY_EXISTS);
    }

    @Test
    void collectorCollectionNotFoundException_shouldExposeApiMetadata() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Collector collection not found",
                ErrorCode.COLLECTOR_COLLECTION_NOT_FOUND);
    }

    @Test
    void collectorPurchaseNotFoundException_shouldExposeApiMetadata() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Collector purchase not found",
                ErrorCode.COLLECTOR_PURCHASE_NOT_FOUND);
    }

    @Test
    void distributorAlreadyExistsException_shouldExposeApiMetadata() {
        DistributorAlreadyExistsException exception = new DistributorAlreadyExistsException("Bandai", "Japan");

        assertApiExceptionMetadata(exception, HttpStatus.CONFLICT, "Distributor already exists",
                ErrorCode.DISTRIBUTOR_ALREADY_EXISTS);
    }

    @Test
    void distributorNotFoundException_shouldExposeApiMetadata() {
        DistributorNotFoundException exception = new DistributorNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Distributor not found",
                ErrorCode.DISTRIBUTOR_NOT_FOUND);
    }

    @Test
    void figurineEventNotFoundException_shouldExposeApiMetadata() {
        FigurineEventNotFoundException exception = new FigurineEventNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Figurine event not found",
                ErrorCode.FIGURINE_EVENT_NOT_FOUND);
    }

    @Test
    void imageAlreadyExistsException_shouldExposeApiMetadata() {
        ImageAlreadyExistsException exception = new ImageAlreadyExistsException(
                URI.create("https://images.example/pegasus.jpg"));

        assertApiExceptionMetadata(exception, HttpStatus.CONFLICT, "Figurine image already exists",
                ErrorCode.FIGURINE_IMAGE_ALREADY_EXISTS);
    }

    @Test
    void imageNotFoundException_shouldExposeApiMetadata() {
        ImageNotFoundException exception = new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Image not found",
                ErrorCode.FIGURINE_IMAGE_NOT_FOUND);
    }

    @Test
    void figurineImportException_shouldExposeApiMetadata() {
        FigurineImportException exception = new FigurineImportException();

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getTitle()).isEqualTo("Figurine Import Error");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FIGURINE_IMPORT_ERROR);
    }

    @Test
    void figurineNotFoundException_shouldExposeApiMetadata() {
        com.mesofi.mythclothapi.figurines.FigurineNotFoundException exception = new com.mesofi.mythclothapi.figurines.FigurineNotFoundException(
                42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Figurine not found", ErrorCode.FIGURINE_NOT_FOUND);
    }

    @Test
    void permissionAlreadyExistsException_shouldExposeApiMetadata() {
        PermissionAlreadyExistsException exception = new PermissionAlreadyExistsException("read");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getTitle()).isEqualTo("Permission with description 'read' already exists");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_ALREADY_EXISTS);
    }

    @Test
    void permissionNotFoundException_shouldExposeApiMetadataForIdConstructor() {
        PermissionNotFoundException exception = new PermissionNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Permission not found",
                ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void permissionNotFoundException_shouldExposeApiMetadataForMessageConstructor() {
        PermissionNotFoundException exception = new PermissionNotFoundException("Permission not found");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getTitle()).isEqualTo("Permission not found");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void roleAlreadyExistsException_shouldExposeApiMetadata() {
        RoleAlreadyExistsException exception = new RoleAlreadyExistsException("admin");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getTitle()).isEqualTo("Role with description 'admin' already exists");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROLE_ALREADY_EXISTS);
    }

    @Test
    void roleNotFoundException_shouldExposeApiMetadataForIdConstructor() {
        RoleNotFoundException exception = new RoleNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Role not found", ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void roleNotFoundException_shouldExposeApiMetadataForNameConstructor() {
        RoleNotFoundException exception = new RoleNotFoundException("admin");

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Role not found", ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void rolePermissionAlreadyExistsException_shouldExposeApiMetadata() {
        RolePermissionAlreadyExistsException exception = new RolePermissionAlreadyExistsException(1L, 99L);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getTitle()).isEqualTo("Role with ID 1 already has permission 99 assigned.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROLE_PERMISSION_ALREADY_EXISTS);
    }

    @Test
    void storeNotFoundException_shouldExposeApiMetadata() {
        StoreNotFoundException exception = new StoreNotFoundException(42L);

        assertApiExceptionMetadata(exception, HttpStatus.NOT_FOUND, "Store not found", ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    void unexpectedException_shouldExposeApiMetadata() {
        UnexpectedException exception = new UnexpectedException("something blew up");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getTitle()).isEqualTo("Unexpected error occurred, try again later.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNEXPECTED_ERROR);
    }

    @Test
    void integrationException_shouldExposeServiceMetadata_fromHttpStatusConstructor() {
        IntegrationException exception = new IntegrationException(ServiceName.GOOGLE, HttpStatus.BAD_GATEWAY,
                "upstream failed");

        assertThat(exception.getServiceName()).isEqualTo(ServiceName.GOOGLE);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getMessage()).isEqualTo("upstream failed");
    }

    @Test
    void integrationException_shouldExposeServiceMetadata_fromCodeConstructor() {
        IntegrationException exception = new IntegrationException(ServiceName.FACEBOOK, 503, "service unavailable");

        assertThat(exception.getServiceName()).isEqualTo(ServiceName.FACEBOOK);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(exception.getMessage()).isEqualTo("service unavailable");
    }

    private static void assertApiExceptionMetadata(ApiException exception, HttpStatus status, String title,
            ErrorCode errorCode) {
        assertThat(exception.getStatus()).isEqualTo(status);
        assertThat(exception.getTitle()).isEqualTo(title);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    }
}

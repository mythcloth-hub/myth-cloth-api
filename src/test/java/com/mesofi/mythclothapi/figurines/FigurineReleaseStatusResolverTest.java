package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

class FigurineReleaseStatusResolverTest {

    @Test
    void resolve_shouldReturnRumoredWhenDistributorsAreMissingOrEmpty() {
        Figurine figurineWithNullDistributors = new Figurine();
        figurineWithNullDistributors.setDistributors(null);

        Figurine figurineWithEmptyDistributors = new Figurine();
        figurineWithEmptyDistributors.setDistributors(new ArrayList<>());

        assertThat(FigurineReleaseStatusResolver.resolve(figurineWithNullDistributors))
                .isEqualTo(ReleaseStatus.RUMORED);
        assertThat(FigurineReleaseStatusResolver.resolve(figurineWithEmptyDistributors))
                .isEqualTo(ReleaseStatus.RUMORED);
    }

    @Test
    void resolve_shouldReturnRumoredWhenDistributorHasNoDates() {
        Figurine figurine = figurine(distributor(null, null, null));

        assertThat(FigurineReleaseStatusResolver.resolve(figurine)).isEqualTo(ReleaseStatus.RUMORED);
    }

    @Test
    void resolve_shouldReturnPrototypeOrUnreleasedWhenOnlyAnnouncementExists() {
        Figurine prototype = figurine(distributor(LocalDate.now().minusYears(2), null, CurrencyCode.JPY));
        Figurine unreleased = figurine(distributor(LocalDate.now().minusYears(6), null, CurrencyCode.JPY));

        assertThat(FigurineReleaseStatusResolver.resolve(prototype)).isEqualTo(ReleaseStatus.PROTOTYPE);
        assertThat(FigurineReleaseStatusResolver.resolve(unreleased)).isEqualTo(ReleaseStatus.UNRELEASED);
    }

    @Test
    void resolve_shouldReturnUnreleasedAtExactlyFiveYearsSinceAnnouncement() {
        Figurine unreleased = figurine(distributor(LocalDate.now().minusYears(5), null, CurrencyCode.JPY));

        assertThat(FigurineReleaseStatusResolver.resolve(unreleased)).isEqualTo(ReleaseStatus.UNRELEASED);
    }

    @Test
    void resolve_shouldReturnReleasedWhenReleaseDateMatchesToday() {
        Figurine releasedToday = figurine(
                distributor(LocalDate.now().minusYears(1), LocalDate.now(), CurrencyCode.JPY));

        assertThat(FigurineReleaseStatusResolver.resolve(releasedToday)).isEqualTo(ReleaseStatus.RELEASED);
    }

    @Test
    void resolve_shouldReturnAnnouncedOrReleasedBasedOnReleaseDate() {
        Figurine announced = figurine(
                distributor(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1), CurrencyCode.JPY));
        Figurine released = figurine(
                distributor(LocalDate.now().minusYears(1), LocalDate.now().minusDays(1), CurrencyCode.JPY));

        assertThat(FigurineReleaseStatusResolver.resolve(announced)).isEqualTo(ReleaseStatus.ANNOUNCED);
        assertThat(FigurineReleaseStatusResolver.resolve(released)).isEqualTo(ReleaseStatus.RELEASED);
    }

    @Test
    void resolve_shouldReturnAnnouncedOrReleasedBasedOnReleaseDateWithNonJPYCurrency() {
        Figurine announced = figurine(
                distributor(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1), CurrencyCode.USD));
        Figurine released = figurine(
                distributor(LocalDate.now().minusYears(1), LocalDate.now().minusDays(1), CurrencyCode.USD));

        assertThat(FigurineReleaseStatusResolver.resolve(announced)).isEqualTo(ReleaseStatus.ANNOUNCED);
        assertThat(FigurineReleaseStatusResolver.resolve(released)).isEqualTo(ReleaseStatus.RELEASED);
    }

    private Figurine figurine(FigurineDistributor distributor) {
        Figurine figurine = new Figurine();
        figurine.setDistributors(List.of(distributor));
        return figurine;
    }

    private FigurineDistributor distributor(LocalDate announcementDate, LocalDate releaseDate,
            CurrencyCode currencyCode) {
        FigurineDistributor distributor = new FigurineDistributor();
        distributor.setAnnouncementDate(announcementDate);
        distributor.setReleaseDate(releaseDate);
        distributor.setCurrency(currencyCode);
        distributor.setReleaseDateConfirmed(releaseDate != null);
        return distributor;
    }
}

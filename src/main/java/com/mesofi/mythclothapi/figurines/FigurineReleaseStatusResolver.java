package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.PROTOTYPE;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RUMORED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.UNRELEASED;

import java.time.LocalDate;
import java.util.List;

import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

/**
 * Resolves the current {@link ReleaseStatus} of a {@link Figurine} based on its
 * release and announcement dates.
 */
public final class FigurineReleaseStatusResolver {

    /**
     * Number of years after an announcement without a release date before a
     * figurine is considered unreleased.
     */
    private static final int UNRELEASED_THRESHOLD_YEARS = 5;

    /**
     * Resolves the release status of the supplied figurine.
     *
     * @param figurine
     *            the figurine to evaluate
     * @return the resolved release status
     */
    public static ReleaseStatus resolve(Figurine figurine) {
        List<FigurineDistributor> distributors = figurine.getDistributors();

        if (distributors == null || distributors.isEmpty()) {
            return RUMORED;
        }

        FigurineDistributor distributor = distributors.getFirst();

        LocalDate releaseDate = distributor.getReleaseDate();
        LocalDate announcementDate = distributor.getAnnouncementDate();
        LocalDate today = LocalDate.now();

        if (releaseDate == null && announcementDate == null) {
            return RUMORED;
        }

        if (releaseDate == null) {
            return today.getYear() - announcementDate.getYear() >= UNRELEASED_THRESHOLD_YEARS ? UNRELEASED : PROTOTYPE;
        }

        return releaseDate.isAfter(today) ? ANNOUNCED : RELEASED;
    }
}

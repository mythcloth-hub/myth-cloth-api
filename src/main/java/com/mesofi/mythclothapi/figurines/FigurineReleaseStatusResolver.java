package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
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
     * Determines the current release status of a figurine based on its
     * distributors' release and announcement dates.
     * <p>
     * The resolution logic is as follows:
     * <ul>
     * <li>If the figurine has no distributors, it is considered
     * {@link ReleaseStatus#RUMORED}.</li>
     * <li>If the figurine has distributors, the distributor with JPY currency is
     * prioritized; if none exists, the first distributor is used.</li>
     * <li>If both release and announcement dates are null, the status is
     * {@link ReleaseStatus#RUMORED}.</li>
     * <li>If the release date is null but the announcement date exists, the status
     * is {@link ReleaseStatus#UNRELEASED} if the announcement was more than
     * {@value #UNRELEASED_THRESHOLD_YEARS} years ago; otherwise, it is
     * {@link ReleaseStatus#PROTOTYPE}.</li>
     * <li>If the release date exists and is in the future, the status is
     * {@link ReleaseStatus#ANNOUNCED}; if it is in the past or today, the status is
     * {@link ReleaseStatus#RELEASED}.</li>
     * </ul>
     * 
     * @param figurine
     *            the figurine whose release status is to be resolved
     * @return the resolved {@link ReleaseStatus} of the figurine
     */
    public static ReleaseStatus resolve(Figurine figurine) {
        List<FigurineDistributor> distributors = figurine.getDistributors();

        if (distributors == null || distributors.isEmpty()) {
            return RUMORED;
        }

        // Prioritize the distributor with JPY currency; if none exists, use the first
        // distributor
        FigurineDistributor distributor = distributors.stream().filter(fd -> fd.getCurrency().equals(JPY)).findFirst()
                .orElseGet(distributors::getFirst);

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

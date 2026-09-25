package com.mesofi.mythclothapi.figurinedistributions;

import java.time.LocalDate;

public record FigurineDistributorProjection(LocalDate releaseDate, boolean releaseDateConfirmed,
        LocalDate announcementDate, String countryCode) {

}

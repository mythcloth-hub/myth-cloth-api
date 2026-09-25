package com.mesofi.mythclothapi.figurines.repository.projection;

import java.time.LocalDate;

public record FigurineRestockProjection(Long figurineId, LocalDate releaseDate) {
}

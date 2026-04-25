package com.mesofi.mythclothapi.figurines;

public record FigurineFilter(
    String name,
    Long lineUpId,
    Long seriesId,
    Long groupId,
    Boolean metalBody,
    Boolean oce,
    Boolean revival,
    Boolean plainCloth,
    Boolean broken,
    Boolean golden,
    Boolean gold,
    Boolean manga,
    Boolean set,
    Boolean articulable,
    String releaseStatus) {}

package com.mesofi.mythclothapi.figurines.mapper;

import java.util.List;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.distributors.model.Distributor;

public record CatalogContext(
    List<Distributor> distributors,
    List<Distribution> distributions,
    List<LineUp> lineUps,
    List<Series> series,
    List<Group> groups,
    List<Anniversary> anniversaries) {}

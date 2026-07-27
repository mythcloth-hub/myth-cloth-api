package com.mesofi.mythclothapi.stores.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stores")
public class Store extends BaseId {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false, unique = true)
    private String website;

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private boolean active = true;

    // FigurineStore.store
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineStore> figurines = new ArrayList<>();
}

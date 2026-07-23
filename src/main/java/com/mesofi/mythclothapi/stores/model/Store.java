package com.mesofi.mythclothapi.stores.model;

import java.util.ArrayList;
import java.util.Currency;
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

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Currency currency;

    // FigurineStore.store
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineStore> figurines = new ArrayList<>();
}

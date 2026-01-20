package com.mesofi.mythclothapi.figurinedistributions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface FigurineDistributorRepository extends JpaRepository<FigurineDistributor, Long> {
  Optional<FigurineDistributor> findByFigurineAndDistributor(
      Figurine figurine, Distributor distributor);
}

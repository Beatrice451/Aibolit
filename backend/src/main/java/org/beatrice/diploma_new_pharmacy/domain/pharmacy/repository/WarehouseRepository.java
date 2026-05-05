package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    List<Warehouse> findByPharmacyId(Integer pharmacyId);

    List<Warehouse> findByPharmacyIdIsNull();

    @Override
    List<Warehouse> findAll();
}

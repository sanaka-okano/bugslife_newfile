package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Tax;

public interface TaxRepository extends JpaRepository<Tax, Long> {

    List<Tax> findByRate(int rate);

    List<Tax> findAllByRate(int rate);
}

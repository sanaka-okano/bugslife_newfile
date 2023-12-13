package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Tax;

public interface TaxRepository extends JpaRepository<Tax, Long> {

}

package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Tax;
import com.example.repository.TaxRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TaxService {
	@Autowired
	private TaxRepository taxRepository;

	public List<Tax> findAll() {
		return taxRepository.findAll();
	}

	public Tax save(Tax entity) {
		return taxRepository.save(entity);
	}

}

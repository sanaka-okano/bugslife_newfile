package com.example.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.form.TaxForm;
import com.example.model.Tax;
import com.example.repository.TaxRepository;

@Service
@Transactional(readOnly = true)
public class TaxService {
	@Autowired
	private TaxRepository taxRepository;

	List<Tax> taxes = new ArrayList<>();

	public List<Tax> findAll() {
		return taxRepository.findAll();
	}

	public Optional<Tax> findOne(Long id){
		return taxRepository.findById(id);
	}

	//重複表示しない
	public List<Tax> getAllDistinctTaxes(){
		List<Tax> allTaxs = taxRepository.findAll();

		Map<Integer, Tax> distinctTaxes = new LinkedHashMap<>();
		for(Tax tax : allTaxs){
			distinctTaxes.putIfAbsent(tax.getRate(), tax);
		}
		return new ArrayList<>(distinctTaxes.values());
	}

	@Transactional(readOnly = false)
	public void save(List<Tax> taxs) {
		taxRepository.saveAll(taxs);
	}

	
	@Transactional(readOnly = false)
	public List<Tax> generateTaxList(int rate) {
        boolean[] taxIncludeds = {false, true};
        String[] roundings = {TaxForm.FLOOR, TaxForm.ROUND, TaxForm.CEIL};
        
        for (boolean taxIncluded : taxIncludeds) {
            for (String rounding : roundings) {
                taxes.add(new Tax(taxes.size() + 1, rate, taxIncluded, rounding));
            }
        }
		save(taxes);
        return taxes;
    }

	//rateの一致する条件でリストから要素を削除するメソッド
	@Transactional(readOnly = false)
	public void delete(int rate){
		// rate に対応するすべてのエンティティを取得
		List<Tax> taxes = taxRepository.findAllByRate(rate);

		for (Tax tax : taxes) {
			if (!isTaxUsedInOtherEntity(tax.getId())) {
				// 他のエンティティで使用中でない場合は削除
				taxRepository.delete(tax);
			} else {
				// 他のエンティティで使用中の場合は例外をスロー
				throw new IllegalStateException(tax.getId() + " Cannot delete.");
			}
		}
	}

	private boolean isTaxUsedInOtherEntity(Long id) {
		return false;
		// 他のentityで使用中かどうかの判定
		// otherEntityRepository.existsByTaxId(taxId);
	}

	public boolean isRateRegistered(int rate) {
		List<Tax> existingTaxes = taxRepository.findByRate(rate);
        return !existingTaxes.isEmpty();
	}



}

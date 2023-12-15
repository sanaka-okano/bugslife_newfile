package com.example.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.constants.Message;
import com.example.model.Tax;
import com.example.service.TaxService;

@Controller
@RequestMapping("/taxs")
public class TaxController {

	@Autowired
	private TaxService taxService;

	@GetMapping
	public String index(Model model) {
		List<Tax> taxs = taxService.getAllDistinctTaxes();
		model.addAttribute("listtax", taxs);
		return "tax/index";
	}

	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute Tax entity) {
		model.addAttribute("tax", entity);
		return "tax/form";
	}

	@PostMapping
	public String create(@RequestParam(name = "rate") int rate, Model model, @Validated @ModelAttribute Tax entity,
			BindingResult result, RedirectAttributes redirectAttributes) {
		Tax tax = null;
		try {
			if (taxService.isRateRegistered(rate)) {
				redirectAttributes.addFlashAttribute("error", "この数値は既に登録されています。");
				return "redirect:/taxs";
			}
			List<Tax> taxList = taxService.generateTaxList(rate);
			model.addAttribute("taxList", taxList);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/taxs";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/taxs";
		}
	}

	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if(id != null){
			Optional<Tax> tax = taxService.findOne(id);
			model.addAttribute("tax", tax.get());
		}
		return "tax/show";
	}

	@DeleteMapping("/{rate}")
	public String delete(@PathVariable("rate") int rate) {
		taxService.delete(rate);
		
		return "redirect:/taxs";
	}
}

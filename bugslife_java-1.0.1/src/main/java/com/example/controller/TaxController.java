package com.example.controller;

import java.util.List;

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
		List<Tax> taxs = taxService.findAll();
		model.addAttribute("listtax", taxs);
		return "tax/index";
	}

	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute Tax entity) {
		model.addAttribute("tax", entity);
		return "tax/form";
	}

	@PostMapping
	public String create(Model model, @Validated @ModelAttribute Tax entity,
			BindingResult result, RedirectAttributes redirectAttributes) {
		Tax tax = null;
		try {
			tax = taxService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/taxs/" + tax.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/taxs";
		}
	}

	@GetMapping("/{id}/show")
	public String show(Model model, @PathVariable("id") Long id) {
		return "tax/show";
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id) {
		return "redirect:/taxs";
	}
}

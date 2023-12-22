package com.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.service.spi.ServiceException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.OrderDeliveriesDto;
import com.example.constants.Message;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentMethod;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderForm.OrderShippingData;
import com.example.model.Order;
import com.example.model.OrderDeliveries;
import com.example.service.OrderDeliveriesService;
import com.example.service.OrderService;
import com.example.service.ProductService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderDeliveriesService orderDeliveriesService;

	@GetMapping
	public String index(Model model) {
		List<Order> all = orderService.findAll();
		model.addAttribute("listOrder", all);
		return "order/index";
	}

	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if (id != null) {
			Optional<Order> order = orderService.findOne(id);
			model.addAttribute("order", order.get());
		}
		return "order/show";
	}

	@GetMapping(value = "/shipping")
	public String shipping(Model model) {
		OrderShippingData orderShippingData = orderDeliveriesService.getOrderShippingData();
		List<OrderDeliveries> orderShippingList = orderShippingData.getOrderShippingList();
		OrderForm orderForm = new OrderForm();
		model.addAttribute("orderShippingList", orderShippingList);
		model.addAttribute("orderShippingData", orderShippingData);
		model.addAttribute("orderForm", orderForm);
		return "order/shipping";
	}


	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute OrderForm.Create entity) {
		model.addAttribute("order", entity);
		model.addAttribute("products", productService.findAll());
		model.addAttribute("paymentMethods", PaymentMethod.values());
		return "order/create";
	}

	@PostMapping
	public String create(@Validated @ModelAttribute OrderForm.Create entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.create(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@GetMapping("/{id}/edit")
	public String update(Model model, @PathVariable("id") Long id) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				model.addAttribute("order", entity.get());
				model.addAttribute("paymentMethods", PaymentMethod.values());
				model.addAttribute("paymentStatus", PaymentStatus.values());
				model.addAttribute("orderStatus", OrderStatus.values());
			}
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return "order/form";
	}

	@PutMapping
	public String update(@Validated @ModelAttribute Order entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_UPDATE);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				orderService.delete(entity.get());
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_DELETE);
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			throw new ServiceException(e.getMessage());
		}
		return "redirect:/orders";
	}

	@PostMapping("/{id}/payments")
	public String createPayment(@Validated @ModelAttribute OrderForm.CreatePayment entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		try {
			orderService.createPayment(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_PAYMENT_INSERT);
			return "redirect:/orders/" + entity.getOrderId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}


	// shipping checkbox更新
	@PutMapping("/shipping")
    public String updateChecked(@ModelAttribute OrderShippingData orderShippingData, Model model) {
        List<OrderDeliveries> orderShippingList = orderShippingData.getOrderShippingList();

        for (OrderDeliveries orderShipping : orderShippingList) {
            if (orderShipping.isChecked()) {
                try {
                    // チェックされた行のデータを更新
                    orderDeliveriesService.updateShippingList(orderShipping);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
		 // 更新後のデータを再取得して表示
		orderShippingData = orderDeliveriesService.getOrderShippingData();
		model.addAttribute("orderShippingList", orderShippingData.getOrderShippingList());
		model.addAttribute("orderShippingData", orderShippingData);
		model.addAttribute("orderForm", new OrderForm());
		return "order/shipping";
    }

	@PostMapping("/shipping/upload_file")
	public String uploadFile(@RequestParam("file") MultipartFile uploadFile, RedirectAttributes redirectAttributes) {

		if (uploadFile.isEmpty()) {
			// ファイルが存在しない場合
			redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
			return "redirect:/orders/shipping";
		}
		if (!"text/csv".equals(uploadFile.getContentType())) {
			// CSVファイル以外の場合
			redirectAttributes.addFlashAttribute("error", "CSVファイルを選択してください。");
			return "redirect:/orders/shipping";
		}
		try {
			orderDeliveriesService.importCSV(uploadFile);
		} catch (Throwable e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			e.printStackTrace();
			return "redirect:/orders/shipping";
		}

		return "redirect:/orders/shipping";
	}

	//ダウンロード
	@PostMapping("/shipping/download")
	public String download(HttpServletResponse response, RedirectAttributes redirectAttributes) {

		try(OutputStream os = response.getOutputStream()){
			List<OrderDeliveriesDto> dataList = orderDeliveriesService.getNotShippedOrders();
			StringBuilder csvContent = new StringBuilder();
        
        // ヘッダー行
        csvContent.append("Order ID,Shipping Code,Shipping Date,DeliveryDate,DeliveryTimezome\n" + //
        "");
        
        // データ行
        for (OrderDeliveriesDto order : dataList) {
            csvContent.append(order.getId())
						.append(",")
						.append(order.getShippingCode())
						.append(",")
						.append(order.getShippingDate())
						.append(",")
						.append(order.getDeliveryDate())
						.append(",")
						.append(order.getDeliveryTimezome())
						.append("\n");
        }

			String attachment = "attachment; filename=shipping_" + new Date().getTime() + ".csv";
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", attachment);
			os.write(csvContent.toString().getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

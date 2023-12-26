package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

// @RequestMapping("${server.error.path:${error.path:/error}}") // エラーページへのマッピング
@Controller
public class MyErrorController implements ErrorController {
	
	@Value("${server.error.path:${error.path:/error}}")
	private String errorPath;


	@RequestMapping("/error")
	public ModelAndView myErrorHtml(HttpServletRequest request) {
	  // HTTP ステータスを決める
	  // ここでは 404 以外は全部 500 にする
		Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (statusCode != null && statusCode.toString().equals("404")) {
			status = HttpStatus.NOT_FOUND;
		}
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setStatus(status);

		//   エラーの種類によって適切なビューを設定
		if (status == HttpStatus.NOT_FOUND) {
			modelAndView.setViewName("error/404");
		} else {
			modelAndView.setViewName("error/500");
		}

	return modelAndView;
}
}

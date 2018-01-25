package com.kalibrasi.vms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontEnd {

	@RequestMapping("/web/**")
	public String index(){
		return "forward:/index.html";
	}
	
}
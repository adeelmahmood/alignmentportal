package org.jhu.metagenomics.alignmentportal.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/")
	public String list() {
		return "views/home";
	}
}

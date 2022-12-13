package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repo.*;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	/*
	 * @Autowired private UserRepository userRepo;
	 * 
	 * @GetMapping("/test")
	 * 
	 * @ResponseBody public String test() {
	 * 
	 * User user = new User(); user.setName("baba");
	 * user.setEmail("baba@gmail.com"); user.setPassword("Baba@123");
	 * 
	 * userRepo.save(user);
	 * 
	 * return "Working"; }
	 */

	// main content

	@Autowired
	private UserRepository userRepo;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home | Page");
		return "index";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About | Page");
		return "about";
	}

	@RequestMapping("/signup")
	public String signUp(Model model) {
		model.addAttribute("title", "SignUp | Page");
		model.addAttribute("user", new User());
		return "signup";
	}

	// this handler for registering user
	// @PostMapping("/do_register")
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("Not check term and condition field");
				throw new Exception("Not check term and condition field");
			}

			if (result1.hasErrors()) {
				System.out.println("ERROR : " + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_User");
			user.setEnabled(true);
			user.setImageUrl("anu.jpeg");
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			

			System.out.println("Agreement " + agreement);
			System.out.println("User " + user);

			User result = this.userRepo.save(user);
			model.addAttribute("user", new User());

			session.setAttribute("Successfully Registered", "alert-success");
			return "login";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong" + e.getMessage(), "alert-danger"));
			return "login";
		}
	}
	
	
	//handler for custom login
	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login | Page");
		return "login";
	}
	


}

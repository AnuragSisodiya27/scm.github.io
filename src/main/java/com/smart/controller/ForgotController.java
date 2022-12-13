package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repo.UserRepository;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		//generating otp fours digit
		
		
		int otp = random.nextInt(9999);
		
		System.out.println(otp);
		
		//sending otp code
		String subject="OTP From SCM";
		String message=""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"<h3>"
				+"OTP is "
				+"<b>"+otp
				+"</b>"
				+"</h3>"
				+"</div>";
		
		String to = email;
		
		
		boolean flag = this.emailService.sendEmail(to, subject, message);
		
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}else {
			//session.setAttribute("message", "Email id is invalid !!");
			session.setAttribute("message", new Message("Email id is invalid !!","success"));
		
			return "forgot_email_form";
		}
		
	
	}
	
	//verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
		
		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myOtp == otp) {
			//redirected to password change form
			
			User user = this.userRepo.getUserByUserName(email);
			
			if(user==null) {
				//send error message
				session.setAttribute("message", "User does not exist with this email !!..");
				
				return "forgot_email_form";
				
			}else {
				//send change password form
				
				
			}
			
			return "password_change_form";
		}else {
			session.setAttribute("message", "You Have Entered Wrong Otp!!...");
			return "verify_otp";
		}
	}
	
	//change password Hanlder
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		String email = (String) session.getAttribute("email");
		
		User user  = this.userRepo.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepo.save(user);
		
		
		
		return "redirect:/signin?change=password changed successfully...";
	}
	
}

package com.smart.controller;

import java.security.Principal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.razorpay.*;
import com.smart.entity.MyOrder;
import com.smart.repo.OrderRepository;
import com.smart.repo.UserRepository;

@Controller
@RequestMapping("/users")
public class PaymentController {

	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws RazorpayException {
		
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		
		//creating razorpay client
		RazorpayClient razorpay = new RazorpayClient("rzp_test_X52fQcBrNkt0Vc", "6jmjWi3KQy32Fq6wfbXbWxDx");
		
		//generating order
		JSONObject options = new JSONObject();
		options.put("amount", amt*100); //amount and amt in paise
		options.put("currency", "INR");
		options.put("receipt", "txn_0827");
		
		//creating order
		Order order = razorpay.orders.create(options);
		
		System.out.println(order);
		
		//save the data in database
		
		MyOrder myOrder = new MyOrder();
		
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setStatus("Created");
		myOrder.setPaymentId(null);
		myOrder.setUser(this.userRepo.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.orderRepo.save(myOrder);
		
		
		
		return order.toString();
	}
	
	
	//
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){
		
		MyOrder order = this.orderRepo.findByOrderId(data.get("order_id").toString());
		
		order.setPaymentId(data.get("payment_id").toString());
		order.setStatus(data.get("status").toString());
		
		this.orderRepo.save(order);
		System.out.println(data);
		
		return ResponseEntity.ok(Map.of("message","Update Successfully"));
	}
	
}

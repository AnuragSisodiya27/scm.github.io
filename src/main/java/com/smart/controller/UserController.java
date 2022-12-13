package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repo.ContactRepository;
import com.smart.repo.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	

	// common handler for all method
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		User user = userRepo.getUserByUserName(userName);

		// System.out.println("USER "+user);
		model.addAttribute("user", user);

	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User | Dashboard");

		return "normal/userdashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			String name = principal.getName();

			User user = this.userRepo.getUserByUserName(name);

			// image -- processing and uploading file
			if (file.isEmpty()) {
				System.out.println("File is empty");
				//set default image if someone does not upload any image
				contact.setImage("contact.png");
			} else {
				System.out.println(file.getOriginalFilename());
				contact.setImage(file.getOriginalFilename());

				File savedFile = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded!! ");
			}

			// bi-directional mapping
			
			user.getContact().add(contact);
			contact.setUser(user);
			System.out.println("Description "+contact.getDescription());
			this.userRepo.save(user);
			System.out.println("Contact Data "+contact);
			
			// message success
			session.setAttribute("message", new Message("Your contact add Successfully", "success"));

		} catch (Exception e) {

			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();

			// error message success
			session.setAttribute("message", new Message("Something when wrong try again", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer currentPage, Model m,Principal principal) {
		
		m.addAttribute("title","Show Contact");
		
		//one way
		/* String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
		List<Contact> contact = user.getContact(); */
		
		//another way
		String name = principal.getName();
		User user  =this.userRepo.getUserByUserName(name);
		
		//Pageable contain two things one is current page location and other is contact shown on per page
		Pageable pageable = PageRequest.of(currentPage, 5);
		
		Page<Contact> contacts = this.contactRepo.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage",currentPage);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing specific contant details
	 @RequestMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		Optional<Contact> contactOptinal = this.contactRepo.findById(cid);
		Contact contact = contactOptinal.get();
		
		//particular can access only his/her contact only
		model.addAttribute("title", "View Contact Page");
		String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		
		return "normal/contact_details";
	}
	 
	 
	 //delete contact
	 @GetMapping("/delete/{cid}")
	 public String deleteContact(@PathVariable("cid") Integer cid,Principal principal,Model model,HttpSession session) {
		 
		Optional<Contact> contactOptional = this.contactRepo.findById(cid);
		 
		Contact contact = contactOptional.get();
		
		//check user cannot delete other contact via link
		String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
		
		user.getContact().remove(contact) ;
		
		this.userRepo.save(user);
		
		/*if(user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
			this.contactRepo.delete(contact);
		}*/
		
		session.setAttribute("message", new Message("Contact Deleted Sucessfully","success"));
		
		 return "redirect:/user/show-contacts/0";
	 }
	 
	 //update form handler
	 @PostMapping("/update-contact/{cid}")
	 public String updateForm(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		 
		 
		 model.addAttribute("title", "Update Contact");
		 
		 Contact contact = this.contactRepo.findById(cid).get();
		 
		 model.addAttribute("contact", contact);
		 
		 
		 return "normal/update_form";
	 }
	 
	 //update contact handler
	 @RequestMapping(value = "/process-update", method = RequestMethod.POST)
	 public String updateContact(@ModelAttribute Contact contact, Model model, @RequestParam("profileImage") MultipartFile file, HttpSession session,Principal principal) {
		 
		 try {
			 //old contact details  -- means detail of contact before updating it
			Contact oldContactDetail =  this.contactRepo.findById(contact.getCid()).get();
			 
			 //image...
			 if(!file.isEmpty()) {
				 //here file is not empty so we re-write the file and replace it 
				 //delete old photo
				 File deleteFile = new ClassPathResource("static/image").getFile();
				 File file1 = new File(deleteFile,oldContactDetail.getImage());
				 file1.delete();
				 
				 //update new photo
				 File savedFile = new ClassPathResource("static/image").getFile();

					Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				 
					contact.setImage(file.getOriginalFilename());
				 
			 }else {
				 contact.setImage(oldContactDetail.getImage());
			 }
			 
			 User user = this.userRepo.getUserByUserName(principal.getName());
			 contact.setUser(user);
			 this.contactRepo.save(contact);
			 
			 session.setAttribute("message",new Message("Your contact is updated","success"));
			 
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 
		 return "redirect:/user/contact/"+contact.getCid();
	 }
	 
	 
	 //profile handler
	 @GetMapping("/profile")
	 public String yourProfile(Model model) {
		 model.addAttribute("title","Profile Page");
		 return "normal/profile";
	 }
	 
	 //open setting hanlder
	 @GetMapping("/settings")
	 public String openSetting(Model model) {
		 model.addAttribute("title","Setting Page");
		 return "normal/settings";
	 }
	 
	 //change password Handler
	 @PostMapping("/change-password")
	 public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword, Model model,Principal principal, HttpSession session) {
		 
		 String username = principal.getName();
		 User currentUser = this.userRepo.getUserByUserName(username);
		 
		 if( (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) && (newPassword.equals(confirmPassword)) ) {

			 //changing the password
			 
			 currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			 this.userRepo.save(currentUser);
			 
			 //httpsession using for sending a message
			 session.setAttribute("message",new Message("Your Password Has Been Change Successfully","success"));
			 System.out.println("Change Successfully");
		 }else {
			 //error...
			 session.setAttribute("message",new Message("Something went Wrong in Change Password Field","danger"));
			 System.out.println("Remain Same");
			 return "redirect:/user/settings";
		 }
		 
		 
		 
		 
		 
		 return "redirect:/user/index";
	 }
	 
}

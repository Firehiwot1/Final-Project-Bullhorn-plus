package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class SecurityController {

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistrationPage(@Valid
                                          @ModelAttribute("user") User user, BindingResult result,
                                          Model model) {
        model.addAttribute("user", user);
        if (result.hasErrors()) {
            return "registration";
        } else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Created");
        }
        return "redirect:/";
    }

    @GetMapping("/username")
    @ResponseBody
    public String currentUsername(Principal principal) {
        return principal.getName();
    }
    //Users with Admin role can view this page
    @RequestMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("mD5Util", new MD5Util());
        return "admin";
    }


    @RequestMapping("/")
    //This will list all the messages
    public String index(Model model) {
        model.addAttribute("messages", messageRepository.findAll());
        model.addAttribute("mD5Util", new MD5Util());
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/secure")
    public String secure(Principal principal, Model model) {
        User myuser = ((CustomUserDetails)
                ((UsernamePasswordAuthenticationToken) principal)
                        .getPrincipal()).getUser();
        model.addAttribute("myuser", myuser);
        return "secure";
    }

    /* Addition for separate log out page */
    @RequestMapping("/logoutconfirm")
    public String logoutconfirm() {
        return "logoutconfirm";
    }


    @RequestMapping("/home")
    public String listMessage(Model model) {
        model.addAttribute("messages", messageRepository.findAllByUsers(userService.getUser()));
//        model.addAttribute("messages", messageRepository.findAll());
        return "list";
    }

    @RequestMapping("/adminhome")
    public String listMessageForAdmin(Model model) {
        model.addAttribute("messages", messageRepository.findAll());
        return "list";
    }


    @GetMapping("/addnew")
    public String newMessageForm(Model model) {
        model.addAttribute("message", new Message());
        return "newmessageform";
    }

    @PostMapping("/process")
    public String processNewFormPic(@ModelAttribute @Valid Message message, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "newmessageform";
        }
        try {
            Map uploadResult = cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            message.setHeadshot(uploadResult.get("url").toString());
            //The part need to add to a specific user
            User currentUser = userService.getUser();
            Set<User> usersSet = new LinkedHashSet<>();
            usersSet.add(currentUser);
            message.setUsers(usersSet);
            //--End--
            messageRepository.save(message);
        } catch (IOException e) {
            e.printStackTrace();
            return "newmessageform";
        }
        return "redirect:/home";
    }

    @PostMapping("/addnew")
    public String processNewFormText(@ModelAttribute @Valid Message message, BindingResult result) {
        if (result.hasErrors()) {
            return "newmessageform";
        }
        //The part need to add to a specific user
        User currentUser = userService.getUser();
        Set<User> usersSet = new LinkedHashSet<>();
        usersSet.add(currentUser);
        message.setUsers(usersSet);
        //--End--
        messageRepository.save(message);
        return "redirect:/home";
    }

    @GetMapping("/add")
    public String messageForm(Model model) {
        model.addAttribute("message", new Message());
        return "messageform";
    }


    @PostMapping("/add")
//    CHANGE THE MESSAGES
    public String processForm(@ModelAttribute @Valid Message message, BindingResult result) {

        String myUrl;
        if (result.hasErrors()) {
            return "messageform";
        }

        myUrl = messageRepository.findById(message.getId()).get().getHeadshot();
        message.setHeadshot(myUrl);
        //The part need to add to a specific user
        User currentUser = userService.getUser();
        Set<User> usersSet = new LinkedHashSet<>();
        usersSet.add(currentUser);
        message.setUsers(usersSet);
        //--End--
        messageRepository.save(message);
        return "redirect:/home";
    }

    @PostMapping("/addpic")
    //CHANGE THE PICTURES
    public String processPicForm(@ModelAttribute Message message, @RequestParam("file") MultipartFile file, BindingResult result) {
        String title = messageRepository.findById(message.getId()).get().getTitle();
        String author = messageRepository.findById(message.getId()).get().getSentby();
        String date = messageRepository.findById(message.getId()).get().getDate();
        String content = messageRepository.findById(message.getId()).get().getContent();

        if (file.isEmpty()) {
            return "redirect:/update/pic/" + messageRepository.findById(message.getId()).get().getId();
        }

        try {
            Map uploadResult = cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            message.setHeadshot(uploadResult.get("url").toString());
            message.setTitle(title);
            message.setContent(content);
            message.setDate(date);
            message.setSentby(author);

            User currentUser = userService.getUser();
            Set<User> usersSet = new LinkedHashSet<>();
            usersSet.add(currentUser);
            message.setUsers(usersSet);

            messageRepository.save(message);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/update/pic/" + messageRepository.findById(message.getId()).get().getId();
        }
        return "redirect:/home";
    }
    @RequestMapping("/myprofile")
    public String getProfile(Principal principal, Model model) {
        User user = userService.getUser();
        model.addAttribute("user", user);

        return "userProfile";
    }


    @RequestMapping("/detail/{id}")
    public String showMessage(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", messageRepository.findById(id).get());
        return "show";
    }


    @RequestMapping("/update/{id}")
    public String updateMessage(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", this.messageRepository.findById(id).get());
        return "messageform";
    }

    @RequestMapping("/update/pic/{id}")
    public String updatePicOfMessage(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", this.messageRepository.findById(id).get());
        return "messagepicform";
    }


    @RequestMapping("/delete/{id}")
    public String delMessage(@PathVariable("id") long id) {
        this.messageRepository.deleteById(id);
        return "redirect:/home";
    }

}


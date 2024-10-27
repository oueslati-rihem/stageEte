package com.authentifcation.projectpitwo.controller;


import com.authentifcation.projectpitwo.dao.RoleDao;
import com.authentifcation.projectpitwo.dao.UserDao;
import com.authentifcation.projectpitwo.entities.Role;
import com.authentifcation.projectpitwo.entities.User;
import com.authentifcation.projectpitwo.repository.UserRepository;
import com.authentifcation.projectpitwo.service.CloudinaryService;
import com.authentifcation.projectpitwo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;

    @PostConstruct
    public void initRoleAndUser() {
        userService.initRoleAndUser();
    }




    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/bannedUserStatistics")
    public ResponseEntity<?> getBannedUserStatistics() {
        long bannedUserCount = userRepository.countByBanned(true);

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("bannedUserCount", bannedUserCount);

        return ResponseEntity.ok(statistics);
    }




    @GetMapping({"/forAdmin"})
    @PreAuthorize("hasRole('Admin')")
    public String forAdmin(){
        return "This URL is only accessible to the admin";
    }

    @GetMapping({"/forUser"})
    @PreAuthorize("hasRole('User')")
    public String forUser(){
        return "This URL is only accessible to the user";
    }

    @GetMapping({"/forTutor"})
    @PreAuthorize("hasRole('Tutor')")
    public String forTutor(){
        return "This URL is only accessible to the tutor";
    }

    @PostMapping(path = "/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap) {
        return userService.changePassword(requestMap);
    }

    @PutMapping("/updateStudents")
    public User updateStudent(@RequestBody User student) {
        return  userService.updateStudent(student);
    }
    @PreAuthorize("hasRole('Admin') or hasRole('User')  or hasRole('Tutor')")
    @GetMapping("/userDetail")
    public ResponseEntity<User> getUserById() {
        try {
            User user = userService.getUserById();
            return ResponseEntity.ok(user);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/getUsers")
    public List<User> getUsers(){
        return userService.getUsers();
    }
    @PostMapping( "/forgotPassword")
    public ResponseEntity<String> forgetPassword(@RequestBody Map<String, String> requestMap) {
        return userService.forgetPassword(requestMap);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        try {
            userService.updatePassword(username, newPassword);
            return ResponseEntity.ok().body("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password");
        }
    }

    @PostMapping("/activate/{email}")
    public ResponseEntity<String> activateUserAccount(@PathVariable String email) {
        ResponseEntity<String> activationResponse = userService.activateUserAccount(email);
        return activationResponse;
    }

    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        userService.activateAccount(token);
    }
    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/userRoleStatistics")
    public ResponseEntity<?> getUserRoleStatistics() {
        Role tutorRole = roleDao.findByroleName("Tutor"); // Assuming you have a method to find a Role by name
        long tutorCount = userDao.countByRole(tutorRole);

        Role userRole = roleDao.findByroleName("User"); // Assuming you have a method to find a Role by name
        long userCount = userDao.countByRole(userRole);

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("tutor", tutorCount);
        statistics.put("user", userCount); // Changed "User" to "user" to match the key case with "tutor"

        return ResponseEntity.ok(statistics);
    }
    @PostMapping(value = "/registerNewUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerNewUser(@RequestParam("userDataJson") String userDataJson,
                                             @RequestParam("cv") MultipartFile cv,
                                             @RequestParam("image") MultipartFile image) {

        try {
            // Parse the JSON string to a Map<String, String>
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> userData = objectMapper.readValue(userDataJson, new TypeReference<Map<String, String>>() {});

            // Validate CV file type
            if (!isValidCVFileType(cv)) {
                throw new IllegalArgumentException("Invalid CV file type. Only PDF files are allowed.");
            }

            // Check if the content of the CV mentions Spring Boot or Angular
            if (!containsSpringBootOrAngularKeywords(cv)) {
                throw new IllegalArgumentException("The content of the CV does not mention the required components.");
            }

            BufferedImage bi = ImageIO.read(image.getInputStream());
            if (bi == null) {
                throw new IllegalArgumentException("Image non valide!");
            }

            Map result = cloudinaryService.upload(image);
            byte[] cvBytes = cv.getBytes();

            // Build the User object using userData and cv content
            User user = User.builder()
                    .userName(userData.get("userName"))
                    .userFirstName(userData.get("userFirstName"))
                    .userLastName(userData.get("userLastName"))
                    .userPassword(userData.get("userPassword"))
                    .contactNumber(userData.get("contactNumber"))
                    .image((String) result.get("url"))
                    .cv(cvBytes)
                    .build();


            String roleName = userData.get("role");
            return userService.registerNewUser(user, roleName);
        } catch (IOException e) {
            // Handle JSON parsing or file reading errors
            throw new RuntimeException("Failed to parse user data or read CV file.", e);
        } catch (IllegalArgumentException e) {
            // Handle validation errors (e.g., invalid file type or missing keywords)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            throw new RuntimeException("Failed to register new user.", e);
        }
    }

    private boolean isValidCVFileType(MultipartFile cv) {
        return cv.getOriginalFilename() != null && cv.getOriginalFilename().toLowerCase().endsWith(".pdf");
    }

    private boolean containsSpringBootOrAngularKeywords(MultipartFile cv) {
        try (InputStream inputStream = cv.getInputStream()) {
            try (PDDocument document = PDDocument.load(inputStream)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                // Check if text contains Spring Boot or Angular keywords
                return text.contains("Formations") || text.contains("Projet Academique") || text.contains("Compétences Technique");
            }
        } catch (IOException e) {
            // Handle PDF processing errors
            throw new RuntimeException("Failed to process CV file.", e);
        }
    }
}







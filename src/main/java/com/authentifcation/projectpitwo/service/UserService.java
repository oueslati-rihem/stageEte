package com.authentifcation.projectpitwo.service;



import com.authentifcation.projectpitwo.configuration.JwtRequestFilter;
import com.authentifcation.projectpitwo.dao.RoleDao;
import com.authentifcation.projectpitwo.dao.UserDao;
import com.authentifcation.projectpitwo.entities.Role;
import com.authentifcation.projectpitwo.entities.Token;
import com.authentifcation.projectpitwo.entities.User;
import com.authentifcation.projectpitwo.repository.TokenRepository;
import com.authentifcation.projectpitwo.repository.UserRepository;
import com.authentifcation.projectpitwo.serviceInterface.UserInterface;
import com.authentifcation.projectpitwo.util.EmailTemplateName;
import com.authentifcation.projectpitwo.util.EmailUtil;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;




@Service

public class UserService implements UserInterface {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public void initRoleAndUser() {

        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        adminRole.setRoleDescription("Admin role");
        roleDao.save(adminRole);

        Role userRole = new Role();
        userRole.setRoleName("User");
        userRole.setRoleDescription("Default role for newly created record");
        roleDao.save(userRole);

        Role userSecondRole = new Role();
        userSecondRole.setRoleName("Tutor");
        userSecondRole.setRoleDescription("Default role for newly created record");
        roleDao.save(userSecondRole);

        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUserName("admin123");
        adminUser.setUserPassword(getEncodedPassword("admin@pass"));
        adminUser.setUserFirstName("admin");
        adminUser.setUserLastName("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRole(adminRoles);
        userDao.save(adminUser);

        User normalUser = new User();
        normalUser.setId(2);
        normalUser.setUserName("rihem@gmail.com");
        normalUser.setUserPassword(getEncodedPassword("admin@pass"));
        normalUser.setUserFirstName("rihem");
        normalUser.setUserLastName("oueslati");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        normalUser.setRole(userRoles);
        userDao.save(normalUser);

        User normalTutor = new User();
        normalTutor.setId(3);
        normalTutor.setUserName("hadil@gmail.com");
        normalTutor.setUserPassword(getEncodedPassword("admin@pass"));
        normalTutor.setUserFirstName("hadil");
        normalTutor.setUserLastName("labidi");
        Set<Role> tutorRoles = new HashSet<>();
        tutorRoles.add(userSecondRole);
        normalTutor.setRole(tutorRoles);
        userDao.save(normalTutor);

    }

    // public User registerNewUser(User user) {
    //   Role role = roleDao.findById("User").get();
    // Set<Role> userRoles = new HashSet<>();
    //userRoles.add(role);
    //user.setRole(userRoles);
    //user.setUserPassword(getEncodedPassword(user.getUserPassword()));

    //        return userDao.save(user);
    //  }
    public ResponseEntity<?> registerNewUser(User user, String roleName) {
        try {
            // Check if the username already exists
            if (userDao.findByUserName(user.getUserName()).isPresent()) {
                return new ResponseEntity<>("Username already exist", HttpStatus.BAD_REQUEST);
            }

            // Retrieve the role based on the selected roleName
            Role role = roleDao.findById(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            // Assign the role to the user
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(role);
            user.setRole(userRoles);

            // Set initial banned status
            user.setBanned(true);

            // Encode the user password before saving
            user.setUserPassword(getEncodedPassword(user.getUserPassword()));

            // Save the user
            User savedUser = userDao.save(user);


            // Send email with user credentials
            //sendVerificationEmail(savedUser);
            sendValidationEmail(savedUser);

            // Return a success response
            return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

        } catch (Exception ex) {

            // Return an error response
            return new ResponseEntity<>("Error registering user: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private void sendVerificationEmail(User user) {
        try {
            // Get user's email address
            String email = user.getUserName(); // Assuming userName contains the email address

            // Generate email content
            String subject = "Your Credentials";
            String message = "Username: " + user.getUserName() + "\nPassword: " + user.getUserPassword();

            // Send email using emailUtil or any email service
            emailUtil.sendVerificationMail(email, subject, message);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error sending email: " + ex.getMessage());
        }
    }
    public ResponseEntity<String> activateUserAccount(String userEmail) {
        Optional<User> userOptional = userDao.findByUserName(userEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setBanned(false); // Set banned status to false
            userDao.save(user); // Save the updated user
            return new ResponseEntity<>("User account activated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }




    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }







    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            Optional<User> user = userDao.findByUserName(jwtRequestFilter.getCurrentid());

            if (user.isPresent()) {
                String oldPasswordFromRequest = requestMap.get("oldPassword");
                String oldPasswordFromDatabase = user.get().getUserPassword(); // Assuming this retrieves the encrypted password from the database

                // Encrypt the plain-text old password provided by the user
                String encryptedOldPassword = passwordEncoder.encode(oldPasswordFromRequest);

                // Compare the encrypted old password from the request with the one from the database
                if (passwordEncoder.matches(oldPasswordFromRequest, oldPasswordFromDatabase)) {
                    // Old password matches, proceed with updating the password
                    String newPassword = requestMap.get("newPassword");

                    // Encrypt the new password
                    String encryptedNewPassword = passwordEncoder.encode(newPassword);
                    System.out.println(newPassword);
                    // Update user's password with the encrypted new password
                    user.get().setUserPassword(encryptedNewPassword);
                    userDao.save(user.get());
                    return new ResponseEntity<>("Password Updated Successfully", HttpStatus.OK);
                } else {
                    // Old password doesn't match
                    return new ResponseEntity<>("Incorrect Old Password", HttpStatus.BAD_REQUEST);
                }
            } else {
                // User not found
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("SOMETHING_WENT_WRONG", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public User updateStudent(User student) {
        Integer id = student.getId();
        Optional<User> existingUser = userDao.findById(id);
        User std = existingUser.get();

        std.setUserName(student.getUserName());
        std.setUserFirstName(student.getUserFirstName());

        return userDao.save(std);
    }
    public User getUserById() {
        Optional<User> user = userDao.findByUserName(jwtRequestFilter.getCurrentid());
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new NoSuchElementException("User not found with");
        }
    }

    public List<User> getUsers(){
        return (List<User>) userRepository.findAll();
    }

    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        System.out.println("inside the forgot password function");
        try {
            String email = requestMap.get("userName");
            Optional<User> userOptional = userDao.findByUserName(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String userName = user.getUserName();
                emailUtil.forgetMail(userName, " Your Credentials ", user.getUserPassword());
                return new ResponseEntity<>( HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("SOMETHING_WENT_WRONG", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void updatePassword(String username, String newPassword) {
        Optional<User> userOptional = userDao.findByUserName(username);
        if (userOptional.isEmpty()) {
            System.out.println("User not found");
            return; // Return or throw an exception based on your application logic
        }

        User user = userOptional.get();
        String encryptedNewPassword = passwordEncoder.encode(newPassword);
        user.setUserPassword(encryptedNewPassword);
        userRepository.save(user);
    }


    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                // todo exception has to be defined
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setBanned(false);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailUtil.sendEmail(
                user.getUserName(),
                user.getUserFirstName()+user.getUserLastName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }



    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }


    @Override
    public User getUserById(Integer id) {

        return userRepository.findById(id).orElse(null);

    }
}






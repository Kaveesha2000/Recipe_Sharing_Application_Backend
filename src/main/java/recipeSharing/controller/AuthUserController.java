package recipeSharing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import recipeSharing.bean.LoginDTO;
import recipeSharing.bean.ResponseDTO;
import recipeSharing.bean.UserRegistrationRequest;
import recipeSharing.entity.AuthUser;
import recipeSharing.repository.AuthUserRepository;
import recipeSharing.service.JWTService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://172.20.10.3:3000")
@RestController
@RequestMapping("/users")
public class AuthUserController {

    private static final Logger logger = LoggerFactory.getLogger(AuthUserController.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;  // JWT utility class
    private final AuthenticationManager authenticationManager;

    public AuthUserController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder, JWTService jwtService, AuthenticationManager authenticationManager) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /*// Register new user by validating and saving their details
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<AuthUser>> registerUser(@RequestBody UserRegistrationRequest userRequest) {
        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to register user: {}", userRequest.getUsername());

            // Check if the username already exists
            if (authUserRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                logger.warn("Username already exists: {}", userRequest.getUsername());
                responseDTO.setStatusCode(400);
                responseDTO.setMessage("Username already exists");
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            String imageFilename = null;
            if (userRequest.getImage() != null && !userRequest.getImage().isEmpty()) {
                try {
                    // Assuming image is sent as a Base64 string, you can decode it and save as needed
                    byte[] imageBytes = Base64.getDecoder().decode(userRequest.getImage().split(",")[1]);
                    // Save imageBytes to your desired location or database
                    imageFilename = "unique_image_filename"; // Generate or get the filename
                } catch (IllegalArgumentException e) {
                    logger.error("Error processing image for user: {}", userRequest.getUsername(), e);
                    responseDTO.setStatusCode(400);
                    responseDTO.setMessage("Error processing image");
                    return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
                }
            }

            AuthUser newUser = AuthUser.builder()
                    .username(userRequest.getUsername())
                    .email(userRequest.getEmail())
                    .password(passwordEncoder.encode(userRequest.getPassword()))
                    .birthday(userRequest.getBirthday())
                    .image(imageFilename)
                    .active(true)
                    .build();
            authUserRepository.save(newUser);

            logger.info("User registered successfully: {}", userRequest.getUsername());
            responseDTO.setStatusCode(201);
            responseDTO.setMessage("User registered successfully");
            responseDTO.setData(newUser);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error registering user: {}", userRequest.getUsername(), e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while registering the user");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @CrossOrigin(origins = "*") // Adjust origins as necessary
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<AuthUser>> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date birthday,
            @RequestParam(value = "image", required = false) String base64Image) {

        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to register user: {}", username);

            // Check if the username already exists
            if (authUserRepository.findByUsername(username).isPresent()) {
                logger.warn("Username already exists: {}", username);
                responseDTO.setStatusCode(400);
                responseDTO.setMessage("Username already exists");
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            // Create and save new user with the base64 image string if provided
            AuthUser newUser = AuthUser.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .birthday(birthday)
                    .image(base64Image) // Save the base64 string in the image field
                    .active(true)
                    .build();
            authUserRepository.save(newUser);

            logger.info("User registered successfully: {}", username);
            responseDTO.setStatusCode(201);
            responseDTO.setMessage("User registered successfully");
            responseDTO.setData(newUser);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error registering user: {}", username, e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while registering the user");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        ResponseDTO<Map<String, Object>> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to login user: {}", loginDTO.getUsername());

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // Obtain UserDetails from Authentication object
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Fetch AuthUser from the database using the username from UserDetails
                Optional<AuthUser> optionalAuthUser = authUserRepository.findByUsername(userDetails.getUsername());
                if (!optionalAuthUser.isPresent()) {
                    logger.warn("User not found in database: {}", userDetails.getUsername());
                    responseDTO.setStatusCode(404);
                    responseDTO.setMessage("User not found");
                    return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
                }

                AuthUser authUser = optionalAuthUser.get();
                String token = jwtService.generateToken(authUser);  // Generate JWT token

                logger.info("User logged in successfully: {}", loginDTO.getUsername());
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("Login successful");
                responseDTO.setData(Map.of(
                        "user", authUser,
                        "token", token
                ));
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                logger.warn("Invalid credentials for user: {}", loginDTO.getUsername());
                responseDTO.setStatusCode(401);
                responseDTO.setMessage("Invalid credentials");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

        } catch (AuthenticationException e) {
            logger.error("Error during login for user: {}", loginDTO.getUsername(), e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while logging in");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        jwtService.blacklistToken(jwtToken);
        return ResponseEntity.ok("Logged out successfully");
    }
}

package recipeSharing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipeSharing.bean.RecipeDTO;
import recipeSharing.bean.ResponseDTO;
import recipeSharing.entity.AuthUser;
import recipeSharing.entity.Recipe;
import recipeSharing.repository.AuthUserRepository;
import recipeSharing.service.JWTService;
import recipeSharing.service.RecipeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthUserRepository authUserRepository;

    private Optional<AuthUser> authenticateUser(String token) {
        String username = jwtService.extractUsername(token);
        return authUserRepository.findByUsername(username)
                .filter(user -> jwtService.isTokenValid(token, user));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<Recipe>> createRecipe(
            @RequestBody RecipeDTO recipeDTO, // Directly map the request body to RecipeDTO
            @RequestHeader("Authorization") String token) throws JsonProcessingException {

        ResponseDTO<Recipe> responseDTO = new ResponseDTO<>();

        Optional<AuthUser> userOptional = authenticateUser(token);
        if (!userOptional.isPresent()) {
            return createErrorResponse(responseDTO, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        // Set the createdBy field directly
        recipeDTO.setCreatedBy(userOptional.get().getUsername());

        Recipe recipe = convertToEntity(recipeDTO);

        // Assuming imageUrl is the field to store Base64 string
        if (recipeDTO.getImageUrl() != null) {
            recipe.setImageUrl(recipeDTO.getImageUrl()); // Set the Base64 image string
        }

        // Save the recipe to the database
        Recipe createdRecipe = recipeService.addRecipe(recipe);
        responseDTO.setData(createdRecipe);
        responseDTO.setMessage("Recipe created successfully");
        responseDTO.setStatusCode(HttpStatus.CREATED.value());

        logger.info("Recipe created successfully: {}", createdRecipe);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    private Recipe convertToEntity(RecipeDTO recipeDTO) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(recipeDTO, recipe);

        // If there's an image URL (Base64), set it in the Recipe entity
        if (recipeDTO.getImageUrl() != null) {
            recipe.setImageUrl(recipeDTO.getImageUrl()); // Assuming imageUrl field in Recipe
        }

        return recipe;
    }
    private ResponseEntity<ResponseDTO<Recipe>> createErrorResponse(ResponseDTO<Recipe> responseDTO, HttpStatus status, String message) {
        responseDTO.setStatusCode(status.value());
        responseDTO.setMessage(message);
        return new ResponseEntity<>(responseDTO, status);
    }

    @PutMapping("/favorite/{id}")
    public ResponseEntity<?> toggleFavorite(@PathVariable String id, @RequestHeader("Authorization") String token) {
        ResponseDTO<Recipe> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to toggle favorite for recipe ID: {}", id);

            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                logger.warn("Unauthorized access attempt with token: {}", token);
                return createErrorResponse(responseDTO, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }

            Recipe updatedRecipe = recipeService.toggleFavorite(id);
            logger.info("Successfully toggled favorite for recipe ID: {}", id);
            return ResponseEntity.ok(updatedRecipe);
        } catch (Exception e) {
            logger.error("An error occurred while toggling favorite for recipe ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Recipe>> getRecipeById(@PathVariable String id, @RequestHeader("Authorization") String token) {
        ResponseDTO<Recipe> responseDTO = new ResponseDTO<>();
        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized access attempt with invalid or expired token for recipe ID: {}", id);
            responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            responseDTO.setMessage("Invalid or expired token");
            return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
        }

        Optional<Recipe> recipeOptional = recipeService.getRecipeById(id);
        if (recipeOptional.isPresent()) {
            responseDTO.setData(recipeOptional.get());
            responseDTO.setMessage("Recipe retrieved successfully");
            logger.info("Recipe retrieved successfully: {}", recipeOptional.get());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            logger.warn("Recipe not found with ID: {}", id);
            responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
            responseDTO.setMessage("Recipe not found");
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Recipe>> getAllRecipes(@RequestHeader("Authorization") String token) {
        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized access attempt with invalid or expired token for all recipes");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Recipe> recipes = recipeService.getAllRecipes();
        logger.info("All recipes retrieved successfully for user: {}", userOptional.get().getUsername());
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<Recipe>> updateRecipe(
            @PathVariable String id,
            @RequestBody Recipe recipe,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<Recipe> responseDTO = new ResponseDTO<>();

        Optional<AuthUser> userOptional = authenticateUser(token);
        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized recipe update attempt with invalid or expired token for recipe ID: {}", id);
            responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            responseDTO.setMessage("Invalid or expired token");
            return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
        }

        Optional<Recipe> recipeOptional = recipeService.getRecipeById(id);
        if (!recipeOptional.isPresent()) {
            logger.warn("Recipe not found with ID: {}", id);
            responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
            responseDTO.setMessage("Recipe not found");
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        if (!recipeOptional.get().getCreatedBy().equals(userOptional.get().getUsername())) {
            logger.warn("Unauthorized update attempt on recipe ID: {} by user: {}", id, userOptional.get().getUsername());
            responseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
            responseDTO.setMessage("You are not authorized to update this recipe");
            return new ResponseEntity<>(responseDTO, HttpStatus.FORBIDDEN);
        }

        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
        if (updatedRecipe == null) {
            logger.warn("Failed to update recipe with ID: {}", id);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("Failed to update recipe");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        responseDTO.setData(updatedRecipe);
        responseDTO.setMessage("Recipe updated successfully");
        responseDTO.setStatusCode(HttpStatus.OK.value());

        logger.info("Recipe updated successfully with ID: {}", updatedRecipe.getId());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id, @RequestHeader("Authorization") String token) {
        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized recipe deletion attempt with invalid or expired token for recipe ID: {}", id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Recipe> recipeOptional = recipeService.getRecipeById(id);
        if (!recipeOptional.isPresent()) {
            logger.warn("Recipe not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!recipeOptional.get().getCreatedBy().equals(userOptional.get().getUsername())) {
            logger.warn("Unauthorized delete attempt on recipe ID: {} by user: {}", id, userOptional.get().getUsername());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recipeService.deleteRecipe(id);
        logger.info("Recipe deleted successfully: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ResponseDTO<List<Recipe>>> getRecipesByUsername(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<List<Recipe>> responseDTO = new ResponseDTO<>();
        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized access attempt with invalid or expired token for user: {}", username);
            responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            responseDTO.setMessage("Invalid or expired token");
            return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
        }

        List<Recipe> recipes = recipeService.getRecipeByUsername(username);
        if (!recipes.isEmpty()) {
            responseDTO.setData(recipes);
            responseDTO.setMessage("Recipes retrieved successfully");
            logger.info("Recipes retrieved successfully for user {}: {}", username, recipes);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            logger.warn("No recipes found for user: {}", username);
            responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
            responseDTO.setMessage("No recipes found for this user");
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
    }
}


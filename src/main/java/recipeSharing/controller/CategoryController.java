package recipeSharing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipeSharing.entity.Category;
import recipeSharing.entity.AuthUser;
import recipeSharing.service.CategoryService;
import recipeSharing.service.JWTService;
import recipeSharing.repository.AuthUserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthUserRepository authUserRepository;

    // Authenticate the user using JWT token
    private Optional<AuthUser> authenticateUser(String token) {
        String username = jwtService.extractUsername(token);
        return authUserRepository.findByUsername(username)
                .filter(user -> jwtService.isTokenValid(token, user));
    }

    // 1. Create a new category
    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category,
            @RequestHeader("Authorization") String token) {

        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized category creation attempt");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Request received to create a new category by user: {}", userOptional.get().getUsername());

        Category newCategory = categoryService.addCategory(category);

        logger.info("Category created successfully with ID: {}", newCategory.getId());
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    // 2. Get all categories
    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategories(@RequestHeader("Authorization") String token) {

        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized access attempt to fetch all categories");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Request received to fetch all categories by user: {}", userOptional.get().getUsername());

        List<Category> categories = categoryService.getAllCategories();

        logger.info("Fetched {} categories", categories.size());
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // 3. Get a category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized access attempt to fetch category with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Request received to fetch category with ID: {} by user: {}", id, userOptional.get().getUsername());

        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isPresent()) {
            logger.info("Category found with ID: {}", id);
            return new ResponseEntity<>(category.get(), HttpStatus.OK);
        } else {
            logger.warn("Category not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 4. Update an existing category
    @PutMapping("/update/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable String id,
            @RequestBody Category category,
            @RequestHeader("Authorization") String token) {

        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized category update attempt with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Request received to update category with ID: {} by user: {}", id, userOptional.get().getUsername());

        Category updatedCategory = categoryService.updateCategory(id, category);

        if (updatedCategory != null) {
            logger.info("Category updated successfully with ID: {}", updatedCategory.getId());
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        } else {
            logger.warn("Failed to update category. Category not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 5. Delete a category by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        Optional<AuthUser> userOptional = authenticateUser(token);

        if (!userOptional.isPresent()) {
            logger.warn("Unauthorized category deletion attempt with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Request received to delete category with ID: {} by user: {}", id, userOptional.get().getUsername());

        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isPresent()) {
            categoryService.deleteCategory(id);
            logger.info("Category deleted successfully with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("Failed to delete category. Category not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

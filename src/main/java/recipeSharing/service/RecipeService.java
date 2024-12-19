package recipeSharing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipeSharing.entity.Recipe;
import recipeSharing.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    // 1. Create a new recipe
    public Recipe addRecipe(Recipe recipe) {
        return recipeRepository.save(recipe); // Saves the recipe document to MongoDB
    }

    // 2. Read a recipe by ID
    public Optional<Recipe> getRecipeById(String id) {
        return recipeRepository.findById(id); // Fetch a single recipe by ID
    }

    // 2.1. Read all recipes
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll(); // Fetch all recipes
    }

    // 3. Update an existing recipe
    public Recipe updateRecipe(String id, Recipe updatedRecipe) {
        Optional<Recipe> existingRecipeOptional = recipeRepository.findById(id);

        if (existingRecipeOptional.isPresent()) {
            Recipe existingRecipe = existingRecipeOptional.get();

            // Update the recipe details
            existingRecipe.setTitle(updatedRecipe.getTitle());
            existingRecipe.setDescription(updatedRecipe.getDescription());
            existingRecipe.setInstructions(updatedRecipe.getInstructions());
            existingRecipe.setServingSize(updatedRecipe.getServingSize());
            existingRecipe.setIngredients(updatedRecipe.getIngredients());

            return recipeRepository.save(existingRecipe); // Save updated recipe back to MongoDB
        }
        return null; // Handle the case where the recipe doesn't exist
    }


    // 4. Delete a recipe by ID
    public void deleteRecipe(String id) {
        recipeRepository.deleteById(id); // Delete a recipe by ID
    }

    public List<Recipe> getRecipeByUsername(String username) {
        // Assuming you have a repository method that allows you to find a recipe by user
        return recipeRepository.findByCreatedBy(username);
    }

    public Recipe toggleFavorite(String id) throws Exception {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new Exception("Recipe not found"));

        recipe.setFavorite(!recipe.isFavorite());
        return recipeRepository.save(recipe);
    }
}

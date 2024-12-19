package recipeSharing.bean;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import recipeSharing.entity.AuthUser;
import recipeSharing.entity.Category;
import recipeSharing.entity.Ingredient;

import javax.persistence.Id;
import java.io.IOException;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private String id;
    private String title;
    private String description;
    private String instructions;

    private int servingSize;
    private String createdBy;

    private String imageUrl;

    private String category; // Reference to Category

    private AuthUser authUser; // Reference to User

    // Embedded ingredients
    private List<Ingredient> ingredients;

    private boolean isFavorite;
}

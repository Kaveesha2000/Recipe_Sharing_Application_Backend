package recipeSharing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;

@Data
@Builder
@Document(collection = "recipe")
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    @Id
    private String id;
    private String title;
    private String description;
    private String instructions;
    private int servingSize;
    private String createdBy;

    private String imageUrl;

    private String category; // Reference to Category

    @DBRef
    private AuthUser authUser; // Reference to User

    // Embedded ingredients
    private List<Ingredient> ingredients;

    private boolean isFavorite = false;
}

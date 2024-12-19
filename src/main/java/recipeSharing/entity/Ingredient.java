package recipeSharing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Builder
@Document(collection = "ingredient")
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    private String id;
    private String name;
    private String unitOfMeasurement;
}

package recipeSharing.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Builder
@Document(collection = "category")
public class Category {
    @Id
    private String id;
    private String name;
}

package recipeSharing.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Builder
@Document(collection = "groceryRetailer")
public class GroceryRetailer {

    @Id
    private String id;
    private String name;
    private String apiEndPoint;
    private String authenticationCredential;


}

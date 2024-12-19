package recipeSharing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import recipeSharing.entity.Category;

public interface CategoryRepository extends MongoRepository<Category, String> {
}

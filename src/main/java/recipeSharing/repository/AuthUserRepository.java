package recipeSharing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import recipeSharing.entity.AuthUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByUsername(String username);
    List<AuthUser> findAll(); // Use this method instead of getAllUsers
}

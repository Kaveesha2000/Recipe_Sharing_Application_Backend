package recipeSharing.bean;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private String email;
    private String username;
    private String password;
    private Date birthday;
    private String image;
}

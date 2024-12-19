/**
 * Author : rasintha_j
 * Date : 11/2/2024
 * Time : 3:51 PM
 * Project Name : Recipe-Sharing-BE-master
 */

package recipeSharing.bean;

import lombok.Data;

import java.util.Date;

@Data
public class UserRegistrationRequest {
    private String username;
    private String email;
    private String password;
    private Date birthday;
    private String image;
}

package recipeSharing.bean;

import lombok.Data;

@Data
public class ResponseDTO<T> {
    private int statusCode;
    private String message;
    private Object data;
}

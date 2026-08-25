package sumit.ai.ai_engineering.user.GlobalExceptionHandling;


import lombok.experimental.StandardException;

@StandardException
public class UserNotFound extends RuntimeException {

    public UserNotFound(String msg) {
        super(msg);

    }
    
}

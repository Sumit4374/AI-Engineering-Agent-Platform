package sumit.ai.ai_engineering.user.GlobalExceptionHandling;


import lombok.experimental.StandardException;

@StandardException
public class UserAlreadyExists extends RuntimeException {

    public UserAlreadyExists(String msg) {
        super(msg);
    }
    
}

package sumit.ai.ai_engineering.ExceptionHandler;


import lombok.experimental.StandardException;

@StandardException
public class UserNotFound extends RuntimeException {

    public UserNotFound(String msg) {
        super(msg);

    }
    
}

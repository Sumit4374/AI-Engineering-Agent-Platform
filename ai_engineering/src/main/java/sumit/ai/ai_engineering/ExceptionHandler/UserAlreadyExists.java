package sumit.ai.ai_engineering.ExceptionHandler;


import lombok.experimental.StandardException;

@StandardException
public class UserAlreadyExists extends RuntimeException {

    public UserAlreadyExists(String msg) {
        super(msg);
    }
    
}

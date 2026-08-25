package sumit.ai.ai_engineering.ai.Capability.IdResolver;

import java.util.UUID;

public class CheckConversationId {
    public static String check(String conversationId){
        if(conversationId==null || conversationId.isBlank()){
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }
}

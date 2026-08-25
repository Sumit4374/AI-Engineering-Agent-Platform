package sumit.ai.ai_engineering.memory.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.memory.model.MemoryMessage;

/**
 * Fast, thread-safe in-memory memory store.
 * Retains bounded recent messages per conversation.
 */
@Component
public class InMemoryMemoryStore implements MemoryStore {

    private static final int DEFAULT_MAX_STORED_PER_CONVERSATION = 50;

    private final ConcurrentHashMap<String, List<MemoryMessage>> store = new ConcurrentHashMap<>();

    @Override
    public List<MemoryMessage> getRecentMessages(String conversationId, int limit) {
        List<MemoryMessage> list = store.get(conversationId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        synchronized (list) {
            int size = list.size();
            int fromIndex = Math.max(0, size - limit);
            return new ArrayList<>(list.subList(fromIndex, size));
        }
    }

    @Override
    public void saveMessage(String conversationId, MemoryMessage message) {
        store.compute(conversationId, (key, existing) -> {
            List<MemoryMessage> list = existing != null ? existing : Collections.synchronizedList(new ArrayList<>());
            list.add(message);
            if (list.size() > DEFAULT_MAX_STORED_PER_CONVERSATION) {
                list.remove(0);
            }
            return list;
        });
    }

    @Override
    public void setMessages(String conversationId, List<MemoryMessage> messages) {
        List<MemoryMessage> list = Collections.synchronizedList(new ArrayList<>(messages));
        store.put(conversationId, list);
    }

    @Override
    public void clear(String conversationId) {
        store.remove(conversationId);
    }

    @Override
    public boolean exists(String conversationId) {
        List<MemoryMessage> list = store.get(conversationId);
        return list != null && !list.isEmpty();
    }
}

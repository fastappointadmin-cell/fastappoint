package com.fastappoint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ChatLlmHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatLlmHistoryService.class);

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Path logPath;
    private final Object writeLock = new Object();

    public ChatLlmHistoryService(ObjectMapper objectMapper,
                                 @Value("${app.chat.llm.history-log-enabled:true}") boolean enabled,
                                 @Value("${app.chat.llm.history-log-path:logs/chat-llm-history.jsonl}") String logPath) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.logPath = Paths.get(logPath).toAbsolutePath().normalize();
    }

    public void logEvent(String interactionId, String eventType, Map<String, Object> payload) {
        if (!enabled) {
            return;
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("timestamp", OffsetDateTime.now().toString());
        event.put("interactionId", interactionId);
        event.put("eventType", eventType);
        if (payload != null && !payload.isEmpty()) {
            event.putAll(payload);
        }

        try {
            String line = objectMapper.writeValueAsString(event) + System.lineSeparator();
            synchronized (writeLock) {
                Path parent = logPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(
                        logPath,
                        line,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND
                );
            }
        } catch (IOException ex) {
            LOG.warn("Failed to write LLM history log: {}", ex.getMessage());
        }
    }

    public String getLogPath() {
        return logPath.toString();
    }
}

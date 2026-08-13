package com.easy.ai.agent;

import com.easy.ai.config.AiPromptConfig;
import com.easy.ai.entity.History;
import com.easy.ai.tools.ChatMemoryTool;
import com.easy.ai.tools.OrderQueryTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UsedCatAgent {

    private static final Logger log = LoggerFactory.getLogger(UsedCatAgent.class);

    @Resource
    private ChatClient chatClient;

    @Resource
    private ChatMemoryTool chatMemoryTool;

    @Resource
    private AiPromptConfig aiPromptConfig;

    @Resource
    private OrderQueryTools orderQueryTools;

    @Resource
    private VectorStore vectorStore;

    /**
     * 从知识库检索相关内容
     */
    private List<Document> searchKnowledge(String query, int topK) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .build();
            return vectorStore.similaritySearch(request);
        } catch (Exception e) {
            log.warn("知识库检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 构建知识库上下文
     */
    private String buildKnowledgeContext(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【知识库参考资料】\n");
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String title = "";
            if (doc.getMetadata() != null && doc.getMetadata().get("title") != null) {
                title = doc.getMetadata().get("title").toString();
            }
            sb.append(String.format("[%d] %s\n%s\n\n",
                    i + 1,
                    title.isEmpty() ? "知识库片段" : title,
                    doc.getText()));
        }
        sb.append("请根据以上知识库内容回答用户问题，如果知识库中没有相关信息，请使用你已有的知识回答，并告知用户这是通用回答。");
        return sb.toString();
    }

    /**
     * 客服流式对话
     * @param message
     * @param userId
     * @return
     */
    public Flux<String> streamChat(String message, Integer userId) {
        Long sessionId = chatMemoryTool.getOrCreateSession(userId)
                .getSessionId().longValue();

        History userHistory = chatMemoryTool.saveUserMessage(message, sessionId);

        List<Message> chatMessages = chatMemoryTool.loadRecentHistory(sessionId, userHistory.getId())
                .stream()
                .map(h -> "user".equals(h.getRole())
                        ? new UserMessage(h.getContent())
                        : new AssistantMessage(h.getContent()))
                .collect(Collectors.toList());

        List<Document> knowledgeDocs = searchKnowledge(message, 5);
        String knowledgeContext = buildKnowledgeContext(knowledgeDocs);

        String systemPrompt = aiPromptConfig.resolve(aiPromptConfig.getCustomerService())
                + knowledgeContext;

        StringBuilder[] builders = {new StringBuilder()};

        return chatClient
                .prompt(systemPrompt)
                .user(message)
                .messages(chatMessages)
                .tools(orderQueryTools)
                .toolContext(Map.of("userId", userId))
                .stream()
                .content()
                .doOnNext(s -> builders[0].append(s))
                .doOnComplete(() ->
                        chatMemoryTool.saveAssistantMessage(builders[0].toString(), sessionId)
                )
                .doOnError(e -> {
                    log.error("AI 对话异常: {}", e.getMessage());
                    String partial = builders[0].toString();
                    if (!partial.isEmpty()) {
                        chatMemoryTool.saveAssistantMessage(partial, sessionId);
                    }
                })
                .onErrorResume(e ->
                        Flux.just("\n\n[抱歉，网络出现波动，请稍后重试]")
                );
    }


    /**
     * 文本美化ai - 美化商品描述
     */
    public String beautifyDescription(String text) {
        return chatClient
                .prompt()
                .user(u -> u.text("""
                        你是一个专业的二手商品文案优化助手。请帮我优化以下商品描述，使其更加吸引人、流畅自然。
                        要求：
                        1. 保持原文的核心信息和关键事实不变
                        2. 优化语言表达，让描述更有吸引力和购买欲
                        3. 保持简洁，不要过度夸张
                        4. 直接返回优化后的文本，不要加任何解释或前缀

                        原始描述：
                        %s
                        """.formatted(text)))
                .call()
                .content();
    }
}

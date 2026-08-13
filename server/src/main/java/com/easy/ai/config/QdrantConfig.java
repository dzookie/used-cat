package com.easy.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
@Slf4j
public class QdrantConfig {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.rest-port:6333}")
    private int restPort;

    @Value("${spring.ai.vectorstore.qdrant.collection-name:vector_store}")
    private String collectionName;

    @Value("${qdrant.vector-size:1536}")
    private int vectorSize;

    @Value("${qdrant.distance:Cosine}")
    private String distance;

    @PostConstruct
    public void initCollection() {
        try {
            String url = String.format("http://%s:%d/collections/%s",
                    host, restPort, collectionName);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Qdrant集合[{}]已存在", collectionName);
                return;
            }
        } catch (Exception e) {
            log.info("Qdrant集合[{}]不存在，准备创建...", collectionName);
        }

        try {
            String createUrl = String.format("http://%s:%d/collections/%s",
                    host, restPort, collectionName);
            Map<String, Object> body = Map.of(
                    "vectors", Map.of(
                            "size", vectorSize,
                            "distance", distance
                    )
            );
            restTemplate.put(createUrl, body);
            log.info("Qdrant集合[{}]创建成功，维度={}，距离={}",
                    collectionName, vectorSize, distance);
        } catch (Exception e) {
            log.error("Qdrant集合创建失败: {}", e.getMessage());
        }
    }
}

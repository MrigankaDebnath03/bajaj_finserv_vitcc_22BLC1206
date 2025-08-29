package com.example.springback.service;

import com.example.springback.dto.GenerateWebhookResponse;
import com.example.springback.dto.GenerateWebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WebhookFlowService {
    private static final Logger log = LoggerFactory.getLogger(WebhookFlowService.class);

    private final WebClient webClient;

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    @Value("${app.generateWebhookUrl}")
    private String generateWebhookUrl;

    @Value("${app.finalQuery:}")
    private String finalQuery;

    public WebhookFlowService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void executeFlow() {
        try {
            log.info("Starting webhook flow for name={}, regNo={}", name, regNo);
            GenerateWebhookRequest req = new GenerateWebhookRequest(name, regNo, email);

            GenerateWebhookResponse resp = webClient.post()
                    .uri(generateWebhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(GenerateWebhookResponse.class)
                    .block();

            if (resp == null) {
                throw new IllegalStateException("Received empty response from generateWebhook");
            }

            String webhook = resp.getWebhook();
            String accessToken = resp.getAccessToken();

            log.info("Received webhook={}, accessToken=<hidden>", webhook);

            // Determine question using last two digits
            int lastTwo = extractLastTwoDigits(regNo);
            boolean isOdd = (lastTwo % 2) == 1;
            if (isOdd) {
                log.info("Assigned Question 1 (odd last two digits). See assignment docs.");
            } else {
                log.info("Assigned Question 2 (even last two digits). See assignment docs.");
            }

            // Get final query either from app.finalQuery or from final-query.sql
            String queryToSend = finalQuery;
            if (!StringUtils.hasText(queryToSend)) {
                Path p = Path.of("final-query.sql");
                if (Files.exists(p)) {
                    queryToSend = Files.readString(p, StandardCharsets.UTF_8).trim();
                    log.info("Read final query from final-query.sql");
                } else {
                    throw new IllegalStateException("No final query provided. Set app.finalQuery or create final-query.sql at project root.");
                }
            }

            Map<String, String> body = Map.of("finalQuery", queryToSend);

            log.info("Submitting final query to webhook URL using Authorization header (JWT)");
            String submitResp = webClient.post()
                    .uri(webhook)
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Submission response: {}", submitResp);

        } catch (Exception e) {
            log.error("Error during webhook flow", e);
            // do not rethrow to avoid failing the whole application startup in some scenarios
        }
    }

    private int extractLastTwoDigits(String reg) {
        if (reg == null) return 0;
        String digits = reg.replaceAll("\\D+", "");
        if (digits.length() == 0) return 0;
        if (digits.length() == 1) return Integer.parseInt(digits);
        String lastTwo = digits.substring(digits.length() - 2);
        return Integer.parseInt(lastTwo);
    }
}

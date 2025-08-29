package com.example.springback.runner;

import com.example.springback.service.WebhookFlowService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private final WebhookFlowService flowService;

    public StartupRunner(WebhookFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // runs automatically at startup
        flowService.executeFlow();
    }
}

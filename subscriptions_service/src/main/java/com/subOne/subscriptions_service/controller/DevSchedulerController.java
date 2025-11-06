package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.scheduler.SchedulerSubscriptionControlDev;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Profile("dev")
@RequestMapping("/scheduler/dev")
public class DevSchedulerController {
    private final SchedulerSubscriptionControlDev schedulerSubscriptionControlDev;

    @PostMapping("/generate/analytic")
    public ResponseEntity<Map<String, String>> generateSubscriptionsAnalytic(){
        schedulerSubscriptionControlDev.generateSubscriptionsAnalytic();
        return ResponseEntity.ok(Map.of("message", "successful generate"));
    }
    @PostMapping("/update/status/subscription")
    public ResponseEntity<Map<String, String>> updateStatusSubscriptions(){
        schedulerSubscriptionControlDev.updateStatusSubscriptions();
        return ResponseEntity.ok(Map.of("message", "successful update"));
    }
    @PostMapping("/send/message/payment/already")
    public ResponseEntity<Map<String, String>> sendMessageWithAlreadyPaymentInfo(){
        schedulerSubscriptionControlDev.sendMessageWithAlreadyPaymentInfo();
        return ResponseEntity.ok(Map.of("message", "successful send"));
    }
    @PostMapping("/send/message/payment")
    public ResponseEntity<Map<String, String>> sendMessageWithPaymentInfo(){
        schedulerSubscriptionControlDev.sendMessageWithPaymentInfo();
        return ResponseEntity.ok(Map.of("message", "successful generate"));
    }
}

package com.example.scheduler.controller;

import com.example.scheduler.service.ChatService;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat Controller")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/patient")
    @PreAuthorize("hasAnyRole('PATIENT')")
    @Operation(summary = "POST /api/chat/patient — chat to communicate with the AI agent")
    public String patientChat(@RequestBody String message, Authentication auth) {
        return chatService.schedule(message, Long.parseLong(auth.getName()));
    }

    @PostMapping("/patient/whatsapp")
    @Operation(summary = "POST /api/chat/patient/whatsapp — chat to communicate with the AI agent")
    public String patientWhatsapp(@RequestParam Map<String, String> requestParams) {
        String from = requestParams.get("From");
        String body = requestParams.get("Body");
        String profileName = requestParams.get("ProfileName");
        System.out.println("Mensaje de " + profileName + " (" + from + "): " + body);
        Body responseBody = new Body.Builder("¡Hola! He recibido tu mensaje: " + body).build();
        Message message = new Message.Builder().body(responseBody).build();
        MessagingResponse twiml = new MessagingResponse.Builder().message(message).build();
        return twiml.toXml();
    }
}

package com.example.scheduler.service.impl;

import com.example.scheduler.Tool.FlowScheduleTool;
import com.example.scheduler.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final HttpSession httpSession;
    private final FlowScheduleTool flowScheduleTool;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder, HttpSession httpSession, FlowScheduleTool flowScheduleTool) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Eres un asistente de una clinica medica y ayudaras a los usuarios a que puedan agendar citas, " +
                        "el flujo para este objetivo es el siguiente -> especialidades -> doctores bajo la especialidad elegida " +
                        "-> horarios bajo el doctor elegido -> crear la cita con el horario escogido y el id del usuario, " +
                        "la primera interaccion con el usuario es ser cordial saludandolo por su nombre " +
                        "y mostrar las especialidades disponibles")
                .build();
        this.httpSession = httpSession;
        this.flowScheduleTool = flowScheduleTool;
    }

    @Override
    public String schedule(String message, Long userId) {
        String id = userId + ":" + httpSession.getId();
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
                .tools(flowScheduleTool)
                .call()
                .content();
    }
}

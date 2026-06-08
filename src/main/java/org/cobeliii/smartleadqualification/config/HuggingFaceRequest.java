package org.cobeliii.smartleadqualification.config;

import java.util.List;

public record HuggingFaceRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Integer max_tokens
) {
    public static HuggingFaceRequest leadQualificationRequest(String model, String userMessage) {
        String systemPrompt = """
                You are a lead qualification assistant.

                Decide whether the user's message is a sales lead.

                A message is a lead if the user shows interest in buying, pricing, scheduling a demo,
                partnership, implementation, consultation, service request, or business opportunity.

                Use only these lead types:
                DEMO_REQUEST,
                PRICING_INQUIRY,
                PARTNERSHIP,
                SUPPORT,
                OTHER

                Use only these urgency levels:
                LOW,
                MEDIUM,
                HIGH

                Urgency rules:
                HIGH: urgent timing, immediate need, ASAP, today, this week, production issue, blocked business.
                MEDIUM: clear business interest but not immediately urgent.
                LOW: vague interest, exploratory, future planning.

                Return valid JSON only.
                Do not include markdown.
                Do not include explanations.

                JSON format:
                {
                  "lead": true,
                  "title": "short title",
                  "type": "DEMO_REQUEST",
                  "urgencyLevel": "MEDIUM",
                  "description": "short summary of the message"
                }

                If it is not a lead, return:
                {
                  "lead": false,
                  "title": null,
                  "type": null,
                  "urgencyLevel": null,
                  "description": null
                }
                """;

        return new HuggingFaceRequest(
                model,
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userMessage)
                ),
                0.0,
                300
        );
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
package org.cobeliii.smartleadqualification.config;

import java.util.List;

public record ChatCompletionResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }

    public String content() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }

        Choice choice = choices.get(0);
        if (choice == null || choice.message() == null || choice.message().content() == null) {
            return "";
        }

        return choice.message().content();
    }
}
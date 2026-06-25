package org.cobeliii.smartleadqualification.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cobeliii.smartleadqualification.config.ChatCompletionResponse;
import org.cobeliii.smartleadqualification.config.HuggingFaceProperties;
import org.cobeliii.smartleadqualification.config.HuggingFaceRequest;
import org.cobeliii.smartleadqualification.config.HuggingFaceService;
import org.cobeliii.smartleadqualification.lead.Lead;
import org.cobeliii.smartleadqualification.lead.LeadQualificationResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final HuggingFaceService huggingFaceService;
    private final HuggingFaceProperties huggingFaceProperties;
    private final ObjectMapper objectMapper;

    public MessageService(
            MessageRepository messageRepository,
            HuggingFaceService huggingFaceService,
            HuggingFaceProperties huggingFaceProperties,
            ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.huggingFaceService = huggingFaceService;
        this.huggingFaceProperties = huggingFaceProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LeadQualificationResult createMessage(Message message) {
        Message savedMessage = messageRepository.save(message);

        HuggingFaceRequest request = HuggingFaceRequest.leadQualificationRequest(
                huggingFaceProperties.chat().model(),
                savedMessage.getMessage()
        );

        ChatCompletionResponse response = getHuggingFaceResponse(request);

        LeadQualificationResult result = parseLeadQualificationResult(response.content());

        if (result.lead()) {
            Lead lead = new Lead(
                    result.title(),
                    result.type(),
                    result.urgencyLevel(),
                    result.description()
            );

            savedMessage.setLead(lead);
            messageRepository.save(savedMessage);
        }

        return result;
    }

    public List<MessageDto> getAllMessages() {
        Pageable pageable = PageRequest.of(0, 10);
        return messageRepository.findAll(pageable)
                .map(m -> new MessageDto(m.getMessage()))
                .stream()
                .toList();
    }

    private ChatCompletionResponse getHuggingFaceResponse(HuggingFaceRequest request) {
        try {
            ChatCompletionResponse response = huggingFaceService.completion(request);

            if (response == null) {
                throw new IllegalStateException("Hugging Face returned a null response.");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "Hugging Face request failed. Status: "
                            + exception.getStatusCode()
                            + ". Response body: "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }

    private LeadQualificationResult parseLeadQualificationResult(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Hugging Face returned an empty message content.");
        }

        try {
            String json = extractJson(content);
            return objectMapper.readValue(json, LeadQualificationResult.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not parse Hugging Face lead qualification response: " + content, exception);
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalStateException("Hugging Face response did not contain valid JSON: " + content);
        }

        return content.substring(start, end + 1);
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
}
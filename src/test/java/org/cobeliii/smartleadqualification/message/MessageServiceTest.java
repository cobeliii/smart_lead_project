package org.cobeliii.smartleadqualification.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cobeliii.smartleadqualification.config.ChatCompletionResponse;
import org.cobeliii.smartleadqualification.config.HuggingFaceProperties;
import org.cobeliii.smartleadqualification.config.HuggingFaceRequest;
import org.cobeliii.smartleadqualification.config.HuggingFaceService;
import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;
import org.cobeliii.smartleadqualification.lead.LeadQualificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private HuggingFaceProperties huggingFaceProperties;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        messageService = new MessageService(
                messageRepository,
                huggingFaceService,
                huggingFaceProperties,
                objectMapper
        );
    }

    @Test
    void createMessage_whenHuggingFaceReturnsLead_createsLeadAndReturnsResult() {
        Message message = new Message("I want to schedule a product demo this week.");
        Message savedMessage = new Message("I want to schedule a product demo this week.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                {
                  "lead": true,
                  "title": "Product demo request",
                  "type": "DEMO_REQUEST",
                  "urgencyLevel": "HIGH",
                  "description": "The customer wants to schedule a product demo this week."
                }
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);
        when(messageRepository.save(savedMessage)).thenReturn(savedMessage);

        LeadQualificationResult result = messageService.createMessage(message);

        assertThat(result.lead()).isTrue();
        assertThat(result.title()).isEqualTo("Product demo request");
        assertThat(result.type()).isEqualTo(Type.DEMO_REQUEST);
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.HIGH);
        assertThat(result.description()).isEqualTo("The customer wants to schedule a product demo this week.");

        assertThat(savedMessage.getLead()).isNotNull();
        assertThat(savedMessage.getLead().getTitle()).isEqualTo("Product demo request");
        assertThat(savedMessage.getLead().getType()).isEqualTo(Type.DEMO_REQUEST);
        assertThat(savedMessage.getLead().getUrgencyLevel()).isEqualTo(UrgencyLevel.HIGH);
        assertThat(savedMessage.getLead().getDescription()).isEqualTo("The customer wants to schedule a product demo this week.");
        assertThat(savedMessage.getLead().getMessage()).isSameAs(savedMessage);

        verify(messageRepository).save(message);
        verify(messageRepository).save(savedMessage);
        verify(huggingFaceService).completion(any(HuggingFaceRequest.class));
    }

    @Test
    void createMessage_whenHuggingFaceReturnsNotLead_doesNotCreateLeadAndReturnsResult() {
        Message message = new Message("Hello, thanks for your help.");
        Message savedMessage = new Message("Hello, thanks for your help.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                {
                  "lead": false,
                  "title": null,
                  "type": null,
                  "urgencyLevel": null,
                  "description": null
                }
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        LeadQualificationResult result = messageService.createMessage(message);

        assertThat(result.lead()).isFalse();
        assertThat(result.title()).isNull();
        assertThat(result.type()).isNull();
        assertThat(result.urgencyLevel()).isNull();
        assertThat(result.description()).isNull();

        assertThat(savedMessage.getLead()).isNull();

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
        verify(huggingFaceService).completion(any(HuggingFaceRequest.class));
    }

    @Test
    void createMessage_sendsExpectedHuggingFaceRequest() {
        Message message = new Message("Can I get pricing for your service?");
        Message savedMessage = new Message("Can I get pricing for your service?");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                {
                  "lead": true,
                  "title": "Pricing inquiry",
                  "type": "PRICING_INQUIRY",
                  "urgencyLevel": "MEDIUM",
                  "description": "The customer is asking for pricing."
                }
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);
        when(messageRepository.save(savedMessage)).thenReturn(savedMessage);

        messageService.createMessage(message);

        ArgumentCaptor<HuggingFaceRequest> requestCaptor = ArgumentCaptor.forClass(HuggingFaceRequest.class);
        verify(huggingFaceService).completion(requestCaptor.capture());

        HuggingFaceRequest request = requestCaptor.getValue();

        assertThat(request.model()).isEqualTo("test-model");
        assertThat(request.temperature()).isEqualTo(0.0);
        assertThat(request.max_tokens()).isEqualTo(300);
        assertThat(request.messages()).hasSize(2);
        assertThat(request.messages().get(0).role()).isEqualTo("system");
        assertThat(request.messages().get(1).role()).isEqualTo("user");
        assertThat(request.messages().get(1).content()).isEqualTo("Can I get pricing for your service?");
    }

    @Test
    void createMessage_whenResponseContainsJsonSurroundedByText_extractsJsonAndReturnsResult() {
        Message message = new Message("We need implementation help.");
        Message savedMessage = new Message("We need implementation help.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                Here is the qualification result:
                {
                  "lead": true,
                  "title": "Implementation help",
                  "type": "OTHER",
                  "urgencyLevel": "MEDIUM",
                  "description": "The customer needs implementation help."
                }
                Thank you.
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);
        when(messageRepository.save(savedMessage)).thenReturn(savedMessage);

        LeadQualificationResult result = messageService.createMessage(message);

        assertThat(result.lead()).isTrue();
        assertThat(result.title()).isEqualTo("Implementation help");
        assertThat(result.type()).isEqualTo(Type.OTHER);
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.MEDIUM);
        assertThat(result.description()).isEqualTo("The customer needs implementation help.");

        assertThat(savedMessage.getLead()).isNotNull();
        assertThat(savedMessage.getLead().getMessage()).isSameAs(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceReturnsNullResponse_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(null);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hugging Face returned a null response.");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceThrowsRestClientResponseException_wrapsExceptionInIllegalStateException() {
        Message message = new Message("I need pricing.");
        Message savedMessage = new Message("I need pricing.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        RestClientResponseException exception = new RestClientResponseException(
                "Bad request",
                400,
                "Bad Request",
                null,
                "{\"error\":\"invalid request\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenThrow(exception);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Hugging Face request failed.")
                .hasMessageContaining("400 BAD_REQUEST")
                .hasMessageContaining("{\"error\":\"invalid request\"}")
                .hasCause(exception);

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceResponseContentIsNull_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = new ChatCompletionResponse(
                List.of(new ChatCompletionResponse.Choice(
                        new ChatCompletionResponse.Message("assistant", null)
                ))
        );

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hugging Face returned an empty message content.");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceResponseContentIsBlank_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("   ");

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hugging Face returned an empty message content.");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceResponseDoesNotContainJson_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("This is not JSON.");

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hugging Face response did not contain valid JSON: This is not JSON.");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceResponseContainsInvalidJson_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                {
                  "lead": true,
                  "title":
                }
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Could not parse Hugging Face lead qualification response:");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenHuggingFaceResponseContainsUnknownEnumValue_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = chatCompletionResponse("""
                {
                  "lead": true,
                  "title": "Demo request",
                  "type": "INVALID_TYPE",
                  "urgencyLevel": "HIGH",
                  "description": "The customer needs a demo."
                }
                """);

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Could not parse Hugging Face lead qualification response:");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void createMessage_whenResponseHasNoChoices_throwsIllegalStateException() {
        Message message = new Message("I need a demo.");
        Message savedMessage = new Message("I need a demo.");
        savedMessage.setId(1L);

        HuggingFaceProperties.Chat chat = new HuggingFaceProperties.Chat("test-model");

        ChatCompletionResponse response = new ChatCompletionResponse(List.of());

        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(huggingFaceProperties.chat()).thenReturn(chat);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> messageService.createMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hugging Face returned an empty message content.");

        verify(messageRepository).save(message);
        verify(messageRepository, never()).save(savedMessage);
    }

    @Test
    void getAllMessages_returnsFirstTenMessagesAsDtos() {
        Message firstMessage = new Message("First message");
        Message secondMessage = new Message("Second message");

        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstMessage, secondMessage)));

        List<MessageDto> result = messageService.getAllMessages();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(MessageDto::message)
                .containsExactly("First message", "Second message");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void getAllMessages_whenRepositoryReturnsEmptyPage_returnsEmptyList() {
        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<MessageDto> result = messageService.getAllMessages();

        assertThat(result).isEmpty();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void deleteMessage_deletesMessageById() {
        Long id = 1L;

        messageService.deleteMessage(id);

        verify(messageRepository).deleteById(id);
        verifyNoInteractions(huggingFaceService, huggingFaceProperties);
    }

    @Test
    void deleteMessage_whenRepositoryThrowsException_propagatesException() {
        Long id = 1L;

        RuntimeException exception = new RuntimeException("Delete failed");

        doThrow(exception).when(messageRepository).deleteById(id);

        assertThatThrownBy(() -> messageService.deleteMessage(id))
                .isSameAs(exception);

        verify(messageRepository).deleteById(id);
        verifyNoInteractions(huggingFaceService, huggingFaceProperties);
    }

    private ChatCompletionResponse chatCompletionResponse(String content) {
        return new ChatCompletionResponse(
                List.of(new ChatCompletionResponse.Choice(
                        new ChatCompletionResponse.Message("assistant", content)
                ))
        );
    }
}
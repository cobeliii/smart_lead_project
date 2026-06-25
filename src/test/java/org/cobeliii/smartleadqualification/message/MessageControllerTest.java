package org.cobeliii.smartleadqualification.message;

import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;
import org.cobeliii.smartleadqualification.lead.LeadQualificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private MessageService messageService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = MockMvcWebTestClient
                .bindToApplicationContext(webApplicationContext)
                .build();
    }

    @Test
    void createMessage_whenMessageIsBlank_returnsBadRequest() {
        webTestClient.post()
                .uri("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "message": ""
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createMessage_whenMessageIsMissing_returnsBadRequest() {
        webTestClient.post()
                .uri("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void getAllMessages_returnsMessages() {
        List<MessageDto> messages = List.of(
                new MessageDto("First message"),
                new MessageDto("Second message")
        );

        when(messageService.getAllMessages()).thenReturn(messages);

        webTestClient.get()
                .uri("/api/v1/messages")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$[0].message").isEqualTo("First message")
                .jsonPath("$[1].message").isEqualTo("Second message");

        verify(messageService).getAllMessages();
    }

    @Test
    void getAllMessages_whenNoMessagesExist_returnsEmptyArray() {
        when(messageService.getAllMessages()).thenReturn(List.of());

        webTestClient.get()
                .uri("/api/v1/messages")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .json("[]");

        verify(messageService).getAllMessages();
    }

    @Test
    void deleteMessage_deletesMessageById() {
        webTestClient.delete()
                .uri("/api/v1/messages/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(messageService).deleteMessage(1L);
    }
}
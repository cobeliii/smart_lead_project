package org.cobeliii.smartleadqualification.lead;

import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(LeadController.class)
class LeadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private LeadService leadService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = MockMvcWebTestClient
                .bindToApplicationContext(webApplicationContext)
                .build();
    }

    @Test
    void getAllLeads_returnsLeads() {
        List<Lead> leads = List.of(
                new Lead(
                        "Demo request",
                        Type.DEMO_REQUEST,
                        UrgencyLevel.HIGH,
                        "Customer wants to schedule a product demo this week."
                ),
                new Lead(
                        "Pricing question",
                        Type.PRICING_INQUIRY,
                        UrgencyLevel.MEDIUM,
                        "Customer wants more information about pricing."
                )
        );

        when(leadService.getAllLeads()).thenReturn(leads);

        webTestClient.get()
                .uri("/api/v1/leads")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Demo request")
                .jsonPath("$[0].type").isEqualTo("DEMO_REQUEST")
                .jsonPath("$[0].urgencyLevel").isEqualTo("HIGH")
                .jsonPath("$[0].description").isEqualTo("Customer wants to schedule a product demo this week.")
                .jsonPath("$[1].title").isEqualTo("Pricing question")
                .jsonPath("$[1].type").isEqualTo("PRICING_INQUIRY")
                .jsonPath("$[1].urgencyLevel").isEqualTo("MEDIUM")
                .jsonPath("$[1].description").isEqualTo("Customer wants more information about pricing.");

        verify(leadService).getAllLeads();
    }

    @Test
    void getAllLeads_whenNoLeadsExist_returnsEmptyArray() {
        when(leadService.getAllLeads()).thenReturn(List.of());

        webTestClient.get()
                .uri("/api/v1/leads")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .json("[]");

        verify(leadService).getAllLeads();
    }
}
package org.cobeliii.smartleadqualification.lead;

import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;


    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadService(leadRepository);
    }

    @Test
    void createLead_savesLeadAndReturnsSavedLead() {
        Lead lead = new Lead(
                "Demo request",
                Type.DEMO_REQUEST,
                UrgencyLevel.HIGH,
                "Customer wants to schedule a demo this week."
        );

        Lead savedLead = new Lead(
                "Demo request",
                Type.DEMO_REQUEST,
                UrgencyLevel.HIGH,
                "Customer wants to schedule a demo this week."
        );
        savedLead.setId(1L);

        when(leadRepository.save(lead)).thenReturn(savedLead);

        Lead result = leadService.createLead(lead);

        assertThat(result).isSameAs(savedLead);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Demo request");
        assertThat(result.getType()).isEqualTo(Type.DEMO_REQUEST);
        assertThat(result.getUrgencyLevel()).isEqualTo(UrgencyLevel.HIGH);
        assertThat(result.getDescription()).isEqualTo("Customer wants to schedule a demo this week.");

        verify(leadRepository).save(lead);
    }

    @Test
    void getAllLeads_returnsFirstTenLeads() {
        Lead firstLead = new Lead(
                "Demo request",
                Type.DEMO_REQUEST,
                UrgencyLevel.HIGH,
                "Customer wants a demo."
        );

        Lead secondLead = new Lead(
                "Pricing inquiry",
                Type.PRICING_INQUIRY,
                UrgencyLevel.MEDIUM,
                "Customer wants pricing information."
        );

        when(leadRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstLead, secondLead)));

        List<Lead> result = leadService.getAllLeads();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(firstLead, secondLead);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(leadRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void getAllLeads_whenRepositoryReturnsEmptyPage_returnsEmptyList() {
        when(leadRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<Lead> result = leadService.getAllLeads();

        assertThat(result).isEmpty();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(leadRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }
}
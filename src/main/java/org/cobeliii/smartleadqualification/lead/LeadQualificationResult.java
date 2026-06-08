package org.cobeliii.smartleadqualification.lead;

import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;

public record LeadQualificationResult(
        boolean lead,
        String title,
        Type type,
        UrgencyLevel urgencyLevel,
        String description
) {
}
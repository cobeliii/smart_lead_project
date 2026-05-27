package org.cobeliii.smartleadqualification.lead;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository
        extends JpaRepository<Lead, Long> {
}

package org.cobeliii.smartleadqualification.message;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.cobeliii.smartleadqualification.lead.Lead;

@Entity
public class Message {
    @Id
    @SequenceGenerator(name = "message_id_sequence",
             sequenceName = "message_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "message_id_sequence")
    private Long id;
    @NotBlank
    @Column(nullable = false, length = 1500)
    private String message;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private Lead lead;

    public Message() {
    }

    public Message(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;

        if (lead != null) {
            lead.setMessage(this);
        }
    }
}

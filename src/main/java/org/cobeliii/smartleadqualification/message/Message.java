package org.cobeliii.smartleadqualification.message;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Message {
    @Id
    @SequenceGenerator(name = "message_id_sequence",
             sequenceName = "message_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "message_id_sequence")
    private Long id;
    @NotBlank
    @Column(nullable = false, length = 1500)
    private String message;

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
}

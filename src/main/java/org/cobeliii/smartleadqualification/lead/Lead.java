package org.cobeliii.smartleadqualification.lead;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.cobeliii.smartleadqualification.enums.Type;
import org.cobeliii.smartleadqualification.enums.UrgencyLevel;
import org.cobeliii.smartleadqualification.message.Message;


@Entity
public class Lead {
    @Id
    @SequenceGenerator(name = "lead_id_sequence",
            sequenceName = "lead_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "lead_id_sequence")
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgencyLevel urgencyLevel;

    @NotBlank
    @Column(nullable = false, length = 1500)
    private String description;

    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "message_id", nullable = false, unique = true)
    private Message message;

    public Lead() {
    }

    public Lead(String title, Type type, UrgencyLevel urgencyLevel, String description) {
        this.title = title;
        this.type = type;
        this.urgencyLevel = urgencyLevel;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(UrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}

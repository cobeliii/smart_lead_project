package org.cobeliii.smartleadqualification.message;

import jakarta.validation.Valid;
import org.cobeliii.smartleadqualification.lead.LeadQualificationResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public LeadQualificationResult createMessage(@Valid @RequestBody Message message) {
        return messageService.createMessage(message);
    }

    @GetMapping
    public List<MessageDto> getAllMessages() {
        return messageService.getAllMessages();
    }

    @DeleteMapping("{id}")
    public void deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }
}
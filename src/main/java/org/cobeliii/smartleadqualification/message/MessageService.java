package org.cobeliii.smartleadqualification.message;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void createMessage(Message message) {
        messageRepository.save(message);
    }

    public List<MessageDto> getAllMessages() {
        Pageable pageable = PageRequest.of(0, 10);
        return messageRepository.findAll(pageable)
                .map(m -> new MessageDto(m.getMessage()))
                .stream().toList();
    }
}

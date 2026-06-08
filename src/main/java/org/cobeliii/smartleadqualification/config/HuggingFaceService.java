package org.cobeliii.smartleadqualification.config;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/chat/completions")
public interface HuggingFaceService {

    @PostExchange
    ChatCompletionResponse completion(@RequestBody HuggingFaceRequest huggingFaceRequest);
}
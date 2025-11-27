package springai.springai;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@RestController
class ChatController {
    private final ChatClient chatClient;
    private final ChatService chatService;

    ChatController(ChatClient.Builder builder, ChatService chatService, ChatService chatService1) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.chatService = chatService1;
    }

    @PostMapping("/api/chat")
    Output chat(@RequestBody @Valid Input input) {
        String response = chatClient.prompt(input.prompt()).call().content();
        return new Output(response);
    }

//    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> streamChat() {
//        return chatClient.prompt("What is Spring AI?")
//                .stream()
//                .content();
//    }

    @GetMapping("/stream/chat")
    public String streamChatt(Input input) {
        return chatClient.prompt(input.prompt)
                .stream()
                .content()
                .collectList()
                .block()
                .stream()
                .reduce("", (a, b) -> a + b);
    }

    record Input(@NotBlank String prompt) {}
    record Output(String content) {}

}

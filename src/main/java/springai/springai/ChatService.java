package springai.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public void streamChat() {
        chatClient.prompt("What is Spring AI?")
//                .system("You are a helpful assistant.")
//                .user("")
//                .user("what is spring ai")
                .stream()
                .content()
                .subscribe(
                        System.out::print,
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("\nStream completed")
                );
    }
}

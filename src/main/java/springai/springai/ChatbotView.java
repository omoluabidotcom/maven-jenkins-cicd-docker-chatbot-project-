package springai.springai;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route("chat")
@PageTitle("AI Chatbot")
public class ChatbotView extends VerticalLayout {

    private final VerticalLayout messageContainer;
    private TextField messageInput;
    private Button sendButton;
    private final Scroller scroller;
    private final WebClient webClient;

    public ChatbotView() {

        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Layout styling update
        getElement().getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("background-color", "#eceff1");

        Div header = createHeader();

        messageContainer = new VerticalLayout();
        messageContainer.setWidthFull();
        messageContainer.setPadding(true);
        messageContainer.setSpacing(true);
        messageContainer.getStyle()
                .set("background-color", "#eceff1")
                .set("flex-grow", "1");

        scroller = new Scroller(messageContainer);
        scroller.setSizeFull();
        scroller.getStyle()
                .set("padding", "20px")
                .set("background-color", "#eceff1");

        HorizontalLayout inputArea = createInputArea();

        add(header, scroller, inputArea);
        expand(scroller);

        // Greetings — First Bot Message
        addBotMessage("Hello! I'm your AI assistant. How can I help you today?");
    }

    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("background-color", "#0066ff")
                .set("color", "white")
                .set("padding", "16px")
                .set("font-size", "18px")
                .set("font-weight", "700")
                .set("border-bottom", "2px solid #0050cc")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "1000")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px");

        header.add(new Icon(VaadinIcon.AUTOMATION), new Span("AI Chatbot"));
        return header;
    }

    private HorizontalLayout createInputArea() {
        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setPadding(true);
        inputArea.setSpacing(true);

        inputArea.getStyle()
                .set("background-color", "white")
                .set("border-top", "1px solid #ccc")
                .set("padding", "10px")
                .set("position", "sticky")
                .set("bottom", "0")
                .set("z-index", "10");

        messageInput = new TextField();
        messageInput.setPlaceholder("Type your message...");
        messageInput.setWidthFull();
        messageInput.setClearButtonVisible(true);
        messageInput.getStyle()
                .set("border-radius", "20px")
                .set("background-color", "#fafafa")
                .set("padding", "10px 14px");
        messageInput.addKeyPressListener(Key.ENTER, e -> sendMessage());

        sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.getStyle()
                .set("border-radius", "20px")
                .set("padding", "10px");
        sendButton.addClickListener(e -> sendMessage());

        inputArea.add(messageInput, sendButton);
        inputArea.expand(messageInput);

        return inputArea;
    }

    private void sendMessage() {
        String prompt = messageInput.getValue().trim();
        if (prompt.isEmpty()) return;

        addUserMessage(prompt);
        messageInput.clear();
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        Div typingIndicator = addTypingIndicator();

        ChatController.Input payload = new ChatController.Input(prompt);

        webClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ChatController.Output.class)
                .subscribe(
                        output -> getUI().ifPresent(ui -> ui.access(() -> {
                            messageContainer.remove(typingIndicator);
                            addBotMessage(output.content());
                            restoreInput();
                        })),
                        error -> getUI().ifPresent(ui -> ui.access(() -> {
                            System.out.println("Errorrrrrrrrrrrrrrrrrrrrrr: " + error.getMessage());
                            messageContainer.remove(typingIndicator);
                            Notification.show("Error: Unable to get response.", 3000, Notification.Position.TOP_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            restoreInput();
                        }))
                );
    }

    private void restoreInput() {
        messageInput.setEnabled(true);
        sendButton.setEnabled(true);
        messageInput.focus();
    }

    private void addUserMessage(String message) {
        messageContainer.add(createMessageBubble(message, true));
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        messageContainer.add(createMessageBubble(message, false));
        scrollToBottom();
    }

    private Div createMessageBubble(String message, boolean isUser) {
        Div bubble = new Div();
        bubble.getStyle()
                .set("max-width", "80%")
                .set("padding", "14px 18px")
                .set("border-radius", "18px")
                .set("margin-bottom", "10px")
                .set("line-height", "1.4")
                .set("word-break", "break-word")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.1)");

        if (isUser) {
            bubble.getStyle()
                    .set("background-color", "#0066ff")
                    .set("color", "white")
                    .set("margin-left", "auto")
                    .set("border", "none");
        } else {
            bubble.getStyle()
                    .set("background-color", "white")
                    .set("color", "#333")
                    .set("margin-right", "auto")
                    .set("border", "1px solid #dcdcdc");
        }

        Span msg = new Span(message);
        msg.getStyle().set("white-space", "pre-wrap");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Span time = new Span(timestamp);
        time.getStyle()
                .set("font-size", "10px")
                .set("opacity", "0.7")
                .set("display", "block")
                .set("margin-top", "6px");

        bubble.add(msg, time);
        return bubble;
    }

    private Div addTypingIndicator() {
        Div typingDiv = new Div();
        typingDiv.getStyle()
                .set("padding", "10px")
                .set("margin-bottom", "10px")
                .set("color", "#777")
                .set("font-style", "italic");

        Span dots = new Span("AI is typing...");
        dots.getStyle()
                .set("animation", "blink 1.5s infinite")
                .set("opacity", "0.8");

        typingDiv.getElement().executeJs(
                "this.animate([{opacity:0.4},{opacity:1}],{duration:600,iterations:Infinity});");

        typingDiv.add(dots);
        messageContainer.add(typingDiv);
        scrollToBottom();
        return typingDiv;
    }

    private void scrollToBottom() {
        getUI().ifPresent(ui -> ui.beforeClientResponse(scroller, ctx ->
                scroller.getElement().executeJs("this.scrollTop = this.scrollHeight;")
        ));
    }
}

//package springai.springai;
//
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.Icon;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.orderedlayout.Scroller;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.component.Key;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.http.MediaType;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Route("chat")
//@PageTitle("AI Chatbot")
//public class ChatbotView extends VerticalLayout {
//
//    private final VerticalLayout messageContainer;
//    private TextField messageInput;
//    private Button sendButton;
//    private final Scroller scroller;
//    private final WebClient webClient;
//
//    public ChatbotView() {
//        this.webClient = WebClient.builder()
//                .baseUrl("http://localhost:8080")
//                .build();
//
//        setSizeFull();
//        setPadding(false);
//        setSpacing(false);
//
//        Div header = createHeader();
//
//        messageContainer = new VerticalLayout();
//        messageContainer.setPadding(true);
//        messageContainer.setSpacing(true);
//        messageContainer.getStyle()
//                .set("background-color", "#f5f5f5")
//                .set("flex-grow", "1");
//
//        scroller = new Scroller(messageContainer);
//        scroller.setSizeFull();
//        scroller.getStyle()
//                .set("padding", "var(--lumo-space-m)")
//                .set("background-color", "#f5f5f5");
//
//        HorizontalLayout inputArea = createInputArea();
//
//        add(header, scroller, inputArea);
//        expand(scroller);
//
//        addBotMessage("Hello! I'm your AI assistant. How can I help you today?");
//    }
//
//    private Div createHeader() {
//        Div header = new Div();
//        header.addClassName("chat-header");
//        header.getStyle()
//                .set("background-color", "var(--lumo-primary-color)")
//                .set("color", "white")
//                .set("padding", "var(--lumo-space-m)")
//                .set("font-size", "var(--lumo-font-size-l)")
//                .set("font-weight", "bold")
//                .set("display", "flex")
//                .set("align-items", "center")
//                .set("gap", "var(--lumo-space-s)");
//
//        Icon robotIcon = new Icon(VaadinIcon.AUTOMATION);
//        Span title = new Span("AI Chatbot");
//        header.add(robotIcon, title);
//
//        return header;
//    }
//
//    private HorizontalLayout createInputArea() {
//        HorizontalLayout inputArea = new HorizontalLayout();
//        inputArea.setWidthFull();
//        inputArea.setPadding(true);
//        inputArea.setSpacing(true);
//        inputArea.getStyle()
//                .set("background-color", "white")
//                .set("border-top", "1px solid var(--lumo-contrast-10pct)");
//
//        messageInput = new TextField();
//        messageInput.setPlaceholder("Type your message...");
//        messageInput.setWidthFull();
//        messageInput.setClearButtonVisible(true);
//        messageInput.addKeyPressListener(Key.ENTER, e -> sendMessage());
//
//        sendButton = new Button("Send", new Icon(VaadinIcon.PAPERPLANE));
//        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        sendButton.addClickListener(e -> sendMessage());
//
//        inputArea.add(messageInput, sendButton);
//        inputArea.expand(messageInput);
//
//        return inputArea;
//    }
//
//    private void sendMessage() {
//        final String prompt = messageInput.getValue().trim();
//        if (prompt.isEmpty()) {
//            return;
//        }
//
//        addUserMessage(prompt);
//
//        messageInput.clear();
//        messageInput.setEnabled(false);
//        sendButton.setEnabled(false);
//
//        Div typingIndicator = addTypingIndicator();
//
//        ChatController.Input payload = new ChatController.Input(prompt);
//
//        webClient.post()
//                .uri("/api/chat")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(ChatController.Output.class)
//                .subscribe(
//                        output -> getUI().ifPresent(ui -> ui.access(() -> {
//                            // Remove typing indicator
//                            messageContainer.remove(typingIndicator);
//
//                            addBotMessage(output.content());
//
//                            messageInput.setEnabled(true);
//                            sendButton.setEnabled(true);
//                            messageInput.focus();
//                        })),
//                        error -> getUI().ifPresent(ui -> ui.access(() -> {
//                            messageContainer.remove(typingIndicator);
//
//                            Notification notification = Notification.show(
//                                    "Error: Unable to get response. Please try again.",
//                                    3000,
//                                    Notification.Position.TOP_CENTER
//                            );
//                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//
//                            messageInput.setEnabled(true);
//                            sendButton.setEnabled(true);
//                            messageInput.focus();
//                        }))
//                );
//    }
//
//    private void addUserMessage(String message) {
//        Div messageDiv = createMessageBubble(message, true);
//        messageContainer.add(messageDiv);
//        scrollToBottom();
//    }
//
//    private void addBotMessage(String message) {
//        Div messageDiv = createMessageBubble(message, false);
//        messageContainer.add(messageDiv);
//        scrollToBottom();
//    }
//
//    private Div createMessageBubble(String message, boolean isUser) {
//        Div bubble = new Div();
//        bubble.addClassName("message-bubble");
//        bubble.getStyle()
//                .set("max-width", "70%")
//                .set("padding", "var(--lumo-space-m)")
//                .set("border-radius", "var(--lumo-border-radius-l)")
//                .set("margin-bottom", "var(--lumo-space-s)")
//                .set("word-wrap", "break-word");
//
//        if (isUser) {
//            bubble.getStyle()
//                    .set("background-color", "var(--lumo-primary-color)")
//                    .set("color", "white")
//                    .set("margin-left", "auto")
//                    .set("margin-right", "0");
//        } else {
//            bubble.getStyle()
//                    .set("background-color", "white")
//                    .set("color", "var(--lumo-body-text-color)")
//                    .set("margin-left", "0")
//                    .set("margin-right", "auto")
//                    .set("border", "1px solid var(--lumo-contrast-10pct)");
//        }
//
//        Span messageText = new Span(message);
//        messageText.getStyle().set("white-space", "pre-wrap");
//
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
//        Span timestampSpan = new Span(timestamp);
//        timestampSpan.getStyle()
//                .set("font-size", "var(--lumo-font-size-xs)")
//                .set("opacity", "0.7")
//                .set("display", "block")
//                .set("margin-top", "var(--lumo-space-xs)");
//
//        bubble.add(messageText, timestampSpan);
//
//        return bubble;
//    }
//
//    private Div addTypingIndicator() {
//        Div typingDiv = new Div();
//        typingDiv.addClassName("typing-indicator");
//        typingDiv.getStyle()
//                .set("padding", "var(--lumo-space-m)")
//                .set("margin-bottom", "var(--lumo-space-s)");
//
//        Span dots = new Span("AI is typing...");
//        dots.getStyle()
//                .set("color", "var(--lumo-secondary-text-color)")
//                .set("font-style", "italic");
//
//        typingDiv.add(dots);
//        messageContainer.add(typingDiv);
//        scrollToBottom();
//
//        return typingDiv;
//    }
//
//    private void scrollToBottom() {
//        getUI().ifPresent(ui -> ui.beforeClientResponse(scroller, ctx ->
//                scroller.getElement().executeJs("this.scrollTop = this.scrollHeight;")
//        ));
//    }
//}

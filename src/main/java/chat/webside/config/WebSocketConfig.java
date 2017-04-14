package chat.webside.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import chat.webside.interactive.InteractiveEventListener;
import chat.webside.interactive.InteractiveRepository;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/exchange")
                .setTaskScheduler(new DefaultManagedTaskScheduler())
                .setHeartbeatValue(new long[]{0, 20 * 1000});

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
        //registry.addEndpoint("/wswebrtc").withSockJS();
    }

    @Bean
    public InteractiveEventListener interactiveEventListener(SimpMessagingTemplate messagingTemplate) {
        InteractiveEventListener listener = new InteractiveEventListener(messagingTemplate, interactiveRepository());
        return listener;
    }

    @Bean
    InteractiveRepository interactiveRepository() {
        return new InteractiveRepository();
    }

}

package itmo.is.cw.GuitarMatchIS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("#{'${app.websocket.broker-destinations:/topic,/feedbacks}'.split(',')}")
    private List<String> brokerDestinations;

    @Value("${app.websocket.application-destination-prefix:/app/**}")
    private String applicationDestinationPrefix;


    @Value("#{'${app.websocket.allowed-origins:http://localhost:5173}'.split(',')}")
    private List<String> wsAllowedOrigins;

    @SuppressWarnings("null")
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(brokerDestinations.toArray(new String[0]));
        registry.setApplicationDestinationPrefixes(applicationDestinationPrefix);
    }

    @SuppressWarnings("null")
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(wsAllowedOrigins.toArray(new String[0])).withSockJS();
    }
}

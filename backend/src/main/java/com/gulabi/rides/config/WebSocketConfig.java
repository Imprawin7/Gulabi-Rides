package com.gulabi.rides.config;
import com.gulabi.rides.security.JwtService; import io.jsonwebtoken.*; import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration; import org.springframework.messaging.*;
import org.springframework.messaging.simp.config.*; import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.*; import java.util.List;

@Configuration @EnableWebSocketMessageBroker @RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final JwtService jwt; private final TopicGuard guard;
  @Override public void configureMessageBroker(MessageBrokerRegistry r) { r.enableSimpleBroker("/topic", "/queue"); r.setApplicationDestinationPrefixes("/app"); }
  @Override public void registerStompEndpoints(StompEndpointRegistry r) { r.addEndpoint("/ws").setAllowedOriginPatterns("*"); }

  /** CONNECT must carry a valid JWT; SUBSCRIBE and SEND are then checked per destination by TopicGuard. */
  @Override public void configureClientInboundChannel(ChannelRegistration reg) {
    reg.interceptors(new ChannelInterceptor() {
      @Override public Message<?> preSend(Message<?> m, MessageChannel ch) {
        StompHeaderAccessor a = MessageHeaderAccessor.getAccessor(m, StompHeaderAccessor.class);
        if (a == null || a.getCommand() == null) return m;
        switch (a.getCommand()) {
          case CONNECT -> {
            String h = a.getFirstNativeHeader("Authorization");
            if (h == null || !h.startsWith("Bearer ")) throw new MessageDeliveryException("Missing token");
            try {
              Claims c = jwt.parse(h.substring(7));
              a.setUser(new UsernamePasswordAuthenticationToken(c.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + c.get("role", String.class)))));
            } catch (JwtException | IllegalArgumentException e) { throw new MessageDeliveryException("Invalid token"); }
          }
          case SUBSCRIBE -> { if (!guard.canSubscribe(auth(a), a.getDestination())) throw new MessageDeliveryException("Forbidden"); }
          case SEND -> { if (!guard.canSend(auth(a), a.getDestination())) throw new MessageDeliveryException("Forbidden"); }
          default -> { }
        }
        return m;
      }
      private Authentication auth(StompHeaderAccessor a) { return a.getUser() instanceof Authentication x ? x : null; }
    });
  }
}

package bootiful.postgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Map;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class ProducerApplication {

    private final String GROUP = "bootiful-group";

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Bean
    JdbcChannelMessageStore messageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    @Bean
    MessageChannel out(JdbcChannelMessageStore messageStore) {
        return MessageChannels.queue(messageStore, GROUP).get();
    }

    @Bean
    RouterFunction<ServerResponse> producer(MessageChannel out) {
        return route()
                .GET("/send/{name}", request -> {
                    var message = MessageBuilder.withPayload("Hello, " + request.pathVariable("name") +
                            " (" + Instant.now() + ")!").build();
                    var sent = Map.of("sent", out.send(message));
                    return ServerResponse.ok().body(sent);
                })
                .build();
    }
}
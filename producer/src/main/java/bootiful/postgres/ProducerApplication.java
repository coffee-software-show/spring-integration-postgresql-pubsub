package bootiful.postgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import javax.sql.DataSource;
import java.time.Instant;

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
    ApplicationListener<ApplicationReadyEvent> ready(MessageChannel out) {
        return event -> {
            var message = MessageBuilder
                    .withPayload("Hello, world (" + Instant.now() + ")!")
                    .build();
            out.send(message);
        };
    }


}

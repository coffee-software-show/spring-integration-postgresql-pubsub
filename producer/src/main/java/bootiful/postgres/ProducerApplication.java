package bootiful.postgres;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import javax.sql.DataSource;

@SpringBootApplication
public class ProducerApplication {

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
        return MessageChannels.queue(messageStore, "bootiful-group").get();
    }

    @Bean
    ApplicationRunner runner (MessageChannel out) {
        return args -> out.send(MessageBuilder.withPayload("Hello, Postgres").build());
    }

}

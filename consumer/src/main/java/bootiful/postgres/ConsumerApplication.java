package bootiful.postgres;

import org.postgresql.jdbc.PgConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jdbc.channel.PgConnectionSupplier;
import org.springframework.integration.jdbc.channel.PostgresChannelMessageTableSubscriber;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import javax.sql.DataSource;
import java.sql.DriverManager;

@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }


    @Bean
    JdbcChannelMessageStore messageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    @Bean
    PostgresChannelMessageTableSubscriber subscriber(
            DataSourceProperties dsp) {
        var supplier = (PgConnectionSupplier) () -> DriverManager.getConnection(
                        dsp.determineUrl(), dsp.determineUsername(), dsp.determinePassword())
                .unwrap(PgConnection.class);
        return new PostgresChannelMessageTableSubscriber(supplier);
    }

    @Bean
    MessageChannel in(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, "bootiful-group",
                subscriber);
    }

    @Bean
    IntegrationFlow inboundSqlMessagesFlow(MessageChannel in) {
        return IntegrationFlow
                .from(in)
                .handle(message -> System.out.println("got the message " + message.getPayload()))
                .get();
    }

}
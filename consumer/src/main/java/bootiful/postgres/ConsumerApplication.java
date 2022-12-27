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
import org.springframework.messaging.MessageChannel;

import javax.sql.DataSource;
import java.sql.DriverManager;

@SpringBootApplication
public class ConsumerApplication {

    private final String GROUP = "bootiful-group";

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    IntegrationFlow inbound(MessageChannel in) {
        return IntegrationFlow
                .from(in)
                .handle((payload, headers) -> {
                    System.out.println("got " + payload);
                    return null;
                })
                .get();
    }

    @Bean
    JdbcChannelMessageStore messageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    @Bean
    PostgresChannelMessageTableSubscriber subscriber(
            DataSourceProperties dataSourceProperties) {
        var supplier = (PgConnectionSupplier) () -> DriverManager.getConnection(
                        dataSourceProperties.determineUrl(),
                        dataSourceProperties.getUsername(),
                        dataSourceProperties.determinePassword())
                .unwrap(PgConnection.class);
        return new PostgresChannelMessageTableSubscriber(supplier);
    }

    @Bean
    PostgresSubscribableChannel in(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, GROUP,
                subscriber);
    }
}
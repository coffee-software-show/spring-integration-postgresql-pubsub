package bootiful.postgres;

import org.postgresql.jdbc.PgConnection;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jdbc.channel.PgConnectionSupplier;
import org.springframework.integration.jdbc.channel.PostgresChannelMessageTableSubscriber;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import javax.sql.DataSource;
import java.sql.DriverManager;

@SpringBootApplication
@ImportRuntimeHints(PostgresApplication.Hints.class)
public class PostgresApplication {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PostgresApplication.class, args);
    }

}
@Configuration
class IntegrationConfiguration {

    @Bean
    IntegrationFlow inbound (MessageChannel inbound){
        return IntegrationFlow
                .from( inbound )
                .handle(new GenericHandler<Object>() {
                    @Override
                    public Object handle(Object payload, MessageHeaders headers) {
                        System.out.println("got " + payload);
                        return null;
                    }
                })
                .get() ;
    }
}
@Configuration
class JdbcConfiguration {

    private final String groupName = "bootiful-group";

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
    PostgresSubscribableChannel channel(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, this.groupName,
                subscriber);
    }
}
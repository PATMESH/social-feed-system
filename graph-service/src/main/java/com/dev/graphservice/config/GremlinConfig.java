package com.dev.graphservice.config;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "graph.impl", havingValue = "gremlin", matchIfMissing = true)
public class GremlinConfig {

    @Value("${gremlin.host:localhost}")
    private String host;

    @Value("${gremlin.port:8182}")
    private int port;

    @Value("${gremlin.username:#{null}}")
    private String username;

    @Value("${gremlin.password:#{null}}")
    private String password;

    @Value("${gremlin.ssl:false}")
    private boolean ssl;

    @Bean(destroyMethod = "close")
    public Cluster gremlinCluster() {

        Cluster.Builder builder = Cluster.build()
                .addContactPoint(host)
                .port(port);

        if (username != null && password != null) {
            builder.credentials(username, password);
        }

        if (username != null && password != null) {
            builder.credentials(username, password);
        }

        return builder
                .maxConnectionPoolSize(8)
                .minConnectionPoolSize(2)
                .maxInProcessPerConnection(128)
                .minInProcessPerConnection(32)
                .maxSimultaneousUsagePerConnection(256)
                .minSimultaneousUsagePerConnection(64)
                .maxWaitForConnection(10000)
                .maxContentLength(10485760)
                .reconnectInterval(1000)
                .resultIterationBatchSize(512)
                .create();
    }

    @Bean
    public Client gremlinClient(Cluster cluster) {
        return cluster.connect();
    }

    @Bean
    public GraphTraversalSource graphTraversalSource(Cluster cluster) {
        return AnonymousTraversalSource.traversal()
                .withRemote(DriverRemoteConnection.using(cluster, "g"));
    }
}
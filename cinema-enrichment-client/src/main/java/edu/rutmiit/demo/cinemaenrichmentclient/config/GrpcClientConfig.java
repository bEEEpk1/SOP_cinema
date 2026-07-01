package edu.rutmiit.demo.cinemaenrichmentclient.config;

import edu.rutmiit.demo.cinemagrpc.analytics.TicketAnalyticsServiceGrpc;
import edu.rutmiit.demo.cinemagrpc.loyalty.LoyaltyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.analytics.host:localhost}")
    private String analyticsHost;

    @Value("${grpc.analytics.port:9092}")
    private int analyticsPort;

    @Value("${grpc.loyalty.host:localhost}")
    private String loyaltyHost;

    @Value("${grpc.loyalty.port:9091}")
    private int loyaltyPort;

    private ManagedChannel analyticsChannel;
    private ManagedChannel loyaltyChannel;

    @Bean
    public ManagedChannel analyticsGrpcChannel() {
        analyticsChannel = ManagedChannelBuilder
                .forAddress(analyticsHost, analyticsPort)
                .usePlaintext()
                .build();
        log.info("analytics gRPC channel created: {}:{}", analyticsHost, analyticsPort);
        return analyticsChannel;
    }

    @Bean
    public TicketAnalyticsServiceGrpc.TicketAnalyticsServiceBlockingStub ticketAnalyticsBlockingStub() {
        return TicketAnalyticsServiceGrpc.newBlockingStub(analyticsGrpcChannel());
    }

    @Bean
    public ManagedChannel loyaltyGrpcChannel() {
        loyaltyChannel = ManagedChannelBuilder
                .forAddress(loyaltyHost, loyaltyPort)
                .usePlaintext()
                .build();
        log.info("loyalty gRPC channel created for async earning flow: {}:{}", loyaltyHost, loyaltyPort);
        return loyaltyChannel;
    }

    @Bean
    public LoyaltyServiceGrpc.LoyaltyServiceBlockingStub loyaltyServiceBlockingStub() {
        return LoyaltyServiceGrpc.newBlockingStub(loyaltyGrpcChannel());
    }

    @PreDestroy
    public void shutdown() {
        shutdown(analyticsChannel, "analytics");
        shutdown(loyaltyChannel, "loyalty");
    }

    private void shutdown(ManagedChannel channel, String name) {
        if (channel != null) {
            log.info("shutting down {} gRPC channel", name);
            channel.shutdown();
        }
    }
}

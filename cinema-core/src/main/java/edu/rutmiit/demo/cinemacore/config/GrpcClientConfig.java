package edu.rutmiit.demo.cinemacore.config;

import edu.rutmiit.demo.cinemagrpc.loyalty.LoyaltyServiceGrpc;
import edu.rutmiit.demo.cinemagrpc.pricing.PricingServiceGrpc;
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

    @Value("${grpc.client.pricing.host:localhost}")
    private String pricingHost;

    @Value("${grpc.client.pricing.port:9090}")
    private int pricingPort;

    @Value("${grpc.client.loyalty.host:localhost}")
    private String loyaltyHost;

    @Value("${grpc.client.loyalty.port:9091}")
    private int loyaltyPort;

    private ManagedChannel pricingChannel;
    private ManagedChannel loyaltyChannel;

    @Bean
    public ManagedChannel pricingGrpcChannel() {
        pricingChannel = ManagedChannelBuilder
                .forAddress(pricingHost, pricingPort)
                .usePlaintext()
                .build();

        log.info("pricing gRPC channel created: {}:{}", pricingHost, pricingPort);
        return pricingChannel;
    }

    @Bean
    public PricingServiceGrpc.PricingServiceBlockingStub pricingServiceBlockingStub() {
        return PricingServiceGrpc.newBlockingStub(pricingGrpcChannel());
    }

    @Bean
    public ManagedChannel loyaltyGrpcChannel() {
        loyaltyChannel = ManagedChannelBuilder
                .forAddress(loyaltyHost, loyaltyPort)
                .usePlaintext()
                .build();

        log.info("loyalty gRPC channel created: {}:{}", loyaltyHost, loyaltyPort);
        return loyaltyChannel;
    }

    @Bean
    public LoyaltyServiceGrpc.LoyaltyServiceBlockingStub loyaltyServiceBlockingStub() {
        return LoyaltyServiceGrpc.newBlockingStub(loyaltyGrpcChannel());
    }

    @PreDestroy
    public void shutdown() {
        shutdown(pricingChannel, "pricing");
        shutdown(loyaltyChannel, "loyalty");
    }

    private void shutdown(ManagedChannel channel, String name) {
        if (channel != null && !channel.isShutdown()) {
            log.info("closing {} gRPC channel", name);
            channel.shutdown();
        }
    }
}

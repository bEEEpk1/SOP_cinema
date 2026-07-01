package edu.rutmiit.demo.cinemaloyalty.config;

import edu.rutmiit.demo.cinemaloyalty.service.LoyaltyGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcServerLifecycle implements SmartLifecycle {

    private final LoyaltyGrpcService loyaltyGrpcService;

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    private Server server;
    private boolean running;

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(loyaltyGrpcService)
                    .build()
                    .start();
            running = true;
            log.info("cinema-loyalty gRPC server started on port {}", grpcPort);
            log.info("gRPC service: LoyaltyService.NormalizeRequestedPoints(), ApplyPointsOnPayment(), GetBalance(), EarnPointsForBooking(), RollbackBookingPoints()");
        } catch (IOException e) {
            throw new IllegalStateException("Could not start cinema-loyalty gRPC server on port " + grpcPort, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            log.info("stopping cinema-loyalty gRPC server");
            server.shutdown();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}

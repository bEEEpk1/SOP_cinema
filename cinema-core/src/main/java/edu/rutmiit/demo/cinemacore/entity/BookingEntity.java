package edu.rutmiit.demo.cinemacore.entity;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor
public class BookingEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BookingStatus status;

    @Column(name = "reserved_until", nullable = false)
    private OffsetDateTime reservedUntil;

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(name = "payment_reference", length = 128)
    private String paymentReference;

    @Column(name = "loyalty_points_used", nullable = false)
    private Integer loyaltyPointsUsed = 0;
}
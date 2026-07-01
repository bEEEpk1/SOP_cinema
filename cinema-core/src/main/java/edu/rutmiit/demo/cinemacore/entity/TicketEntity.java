package edu.rutmiit.demo.cinemacore.entity;

import edu.rutmiit.demo.cinemaapicontract.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ticket_booking", columnNames = "booking_id"),
        @UniqueConstraint(name = "uk_ticket_number", columnNames = "ticket_number")
})
@Getter @Setter @NoArgsConstructor
public class TicketEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "ticket_number", nullable = false, length = 64)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "qr_code", nullable = false, length = 512)
    private String qrCode;
}
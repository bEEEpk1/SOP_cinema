package edu.rutmiit.demo.cinemacore.entity;

import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "waitlist_entries")
@Getter @Setter @NoArgsConstructor
public class WaitlistEntryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "show_id", nullable = false)
    private Long showId;
    @Column(name = "seat_id")
    private Long seatId;
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WaitlistStatus status = WaitlistStatus.ACTIVE;
}

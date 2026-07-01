package edu.rutmiit.demo.cinemacore.entity;

import edu.rutmiit.demo.cinemaapicontract.enums.SeatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_seat_hall_row_number", columnNames = {"hall_id", "row_number", "seat_number"})
})
@Getter @Setter @NoArgsConstructor
public class SeatEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 32)
    private SeatType seatType;

    @Column(nullable = false)
    private Boolean active = true;
}
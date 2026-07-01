package edu.rutmiit.demo.cinemacore.entity;

import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "halls")
@Getter @Setter @NoArgsConstructor
public class HallEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false, length = 32)
    private HallType hallType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean active = true;
}
package edu.rutmiit.demo.cinemacore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter @Setter @NoArgsConstructor
public class MovieEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "age_rating", nullable = false, length = 32)
    private String ageRating;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(nullable = false)
    private Boolean active = true;
}
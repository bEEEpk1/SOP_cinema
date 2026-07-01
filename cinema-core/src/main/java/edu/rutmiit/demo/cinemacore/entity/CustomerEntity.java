package edu.rutmiit.demo.cinemacore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_customer_phone", columnNames = "phone")
})
@Getter @Setter @NoArgsConstructor
public class CustomerEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean registered;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
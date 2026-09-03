package com.bigbasket;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(name = "password_hash", length = 255) private String passwordHash;
    @Column(nullable = false, length = 20) private String phone;
    @Column(name = "date_of_birth", nullable = false) private LocalDate dateOfBirth;
    @Column(nullable = false, length = 30) private String provider = "LOCAL";
    @Column(name = "provider_subject", unique = true, length = 190) private String providerSubject;

    protected UserEntity() {}
    public UserEntity(String name, String email, String passwordHash, String phone, LocalDate dateOfBirth) { this.name = name; this.email = email; this.passwordHash = passwordHash; this.phone = phone; this.dateOfBirth = dateOfBirth; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
}

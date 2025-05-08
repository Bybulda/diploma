package org.diploma.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "timestamp")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "origin_lat")
    private double startLat;
    @Column(name = "origin_lon")
    private double startLng;
    @Column(name = "destination_lat")
    private double endLat;
    @Column(name = "destination_lon")
    private double endLng;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlockedArea> blockedAreas;
}

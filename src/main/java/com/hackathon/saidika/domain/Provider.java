package com.hackathon.saidika.domain;

import com.hackathon.saidika.service.DistanceCalculator;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Location location;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private double serviceRadiusKm = 25.0;

    @ElementCollection(targetClass = ServiceType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "provider_services", joinColumns = @JoinColumn(name = "provider_id"))
    @Column(name = "service_type", nullable = false)
    private Set<ServiceType> supportedServices = new HashSet<>();

    protected Provider() {
    }

    public Provider(String name, Location location, boolean available, Set<ServiceType> supportedServices) {
        this(name, location, available, supportedServices, 25.0);
    }

    public Provider(String name, Location location, boolean available, Set<ServiceType> supportedServices, double serviceRadiusKm) {
        this.name = name;
        this.location = location;
        this.available = available;
        this.serviceRadiusKm = serviceRadiusKm;
        this.supportedServices = supportedServices == null ? new HashSet<>() : new HashSet<>(supportedServices);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return available;
    }

    public double getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public Set<ServiceType> getSupportedServices() {
        return supportedServices;
    }

    public boolean supports(ServiceType serviceType) {
        return supportedServices.contains(serviceType);
    }

    public boolean isWithinServiceRadius(Location userLocation) {
        if (userLocation == null) {
            return false;
        }
        return DistanceCalculator.calculateKm(userLocation, this.location) <= serviceRadiusKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provider provider = (Provider) o;
        return Objects.equals(id, provider.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

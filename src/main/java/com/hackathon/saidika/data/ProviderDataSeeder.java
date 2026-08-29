package com.hackathon.saidika.data;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.repository.ProviderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class ProviderDataSeeder {

    @Bean
    CommandLineRunner seedProviders(ProviderRepository providerRepository) {
        return args -> {
            if (!providerRepository.findAll().isEmpty()) {
                return;
            }

            List<Provider> providers = List.of(
                    new Provider("Nairobi Jump Centre", new Location(-1.2864, 36.8172), true, Set.of(ServiceType.JUMP_START, ServiceType.TOWING), 12.0),
                    new Provider("Westlands Battery Care", new Location(-1.2576, 36.8037), true, Set.of(ServiceType.JUMP_START), 10.0),
                    new Provider("Karen Tyre Fix", new Location(-1.3180, 36.7069), true, Set.of(ServiceType.TYRE_ASSISTANCE), 9.0),
                    new Provider("Eastleigh Tyre Response", new Location(-1.2681, 36.8794), true, Set.of(ServiceType.TYRE_ASSISTANCE, ServiceType.FUEL_ASSISTANCE), 15.0),
                    new Provider("Kilimani Mechanics", new Location(-1.2807, 36.7893), true, Set.of(ServiceType.MOBILE_MECHANIC, ServiceType.TOWING), 14.0),
                    new Provider("Embakasi Recovery", new Location(-1.3279, 36.8970), true, Set.of(ServiceType.TOWING, ServiceType.FUEL_ASSISTANCE), 18.0),
                    new Provider("Airport Fuel Point", new Location(-1.3196, 36.9260), true, Set.of(ServiceType.FUEL_ASSISTANCE), 16.0),
                    new Provider("CBD Lock & Go", new Location(-1.2833, 36.8167), true, Set.of(ServiceType.LOCKSMITH), 8.0),
                    new Provider("Ngong Lockout Team", new Location(-1.3612, 36.6465), true, Set.of(ServiceType.LOCKSMITH, ServiceType.JUMP_START), 20.0),
                    new Provider("Ruai Road Rescue", new Location(-1.2200, 37.0143), true, Set.of(ServiceType.TOWING, ServiceType.MOBILE_MECHANIC), 22.0),
                    new Provider("Kiserian Assist", new Location(-1.4243, 36.6778), true, Set.of(ServiceType.MOBILE_MECHANIC, ServiceType.FUEL_ASSISTANCE), 25.0),
                    new Provider("Kibera Road Care", new Location(-1.3068, 36.7895), true, Set.of(ServiceType.JUMP_START, ServiceType.TYRE_ASSISTANCE), 11.0),
                    new Provider("Closed Nairobi Tyre", new Location(-1.3900, 36.7200), false, Set.of(ServiceType.TYRE_ASSISTANCE), 6.0),
                    new Provider("Out-of-area Recovery", new Location(-0.9000, 34.6000), true, Set.of(ServiceType.TOWING, ServiceType.JUMP_START), 5.0)
            );

            providerRepository.saveAll(providers);
        };
    }
}

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
                    new Provider("Metro Jump", new Location(51.5034, -0.1196), true, Set.of(ServiceType.JUMP_START, ServiceType.TOWING)),
                    new Provider("North Battery", new Location(51.5200, -0.1400), true, Set.of(ServiceType.JUMP_START)),
                    new Provider("Harbor Tyres", new Location(51.4900, -0.1000), true, Set.of(ServiceType.TYRE_ASSISTANCE)),
                    new Provider("South Tyre Response", new Location(51.4700, -0.0700), true, Set.of(ServiceType.TYRE_ASSISTANCE, ServiceType.FUEL_ASSISTANCE)),
                    new Provider("City Mechanics", new Location(51.5300, -0.1500), true, Set.of(ServiceType.MOBILE_MECHANIC, ServiceType.TOWING)),
                    new Provider("Highway Recovery", new Location(51.5500, -0.1800), true, Set.of(ServiceType.TOWING, ServiceType.FUEL_ASSISTANCE)),
                    new Provider("Quick Fuel", new Location(51.4950, -0.1300), true, Set.of(ServiceType.FUEL_ASSISTANCE)),
                    new Provider("Lock & Go", new Location(51.5100, -0.0900), true, Set.of(ServiceType.LOCKSMITH)),
                    new Provider("West Lockout", new Location(51.4500, -0.1600), true, Set.of(ServiceType.LOCKSMITH, ServiceType.JUMP_START)),
                    new Provider("Rural Rescue", new Location(52.0000, -0.1000), true, Set.of(ServiceType.TOWING, ServiceType.MOBILE_MECHANIC)),
                    new Provider("Closed Service", new Location(51.5000, -0.1000), false, Set.of(ServiceType.JUMP_START, ServiceType.TYRE_ASSISTANCE, ServiceType.LOCKSMITH))
            );

            providerRepository.saveAll(providers);
        };
    }
}

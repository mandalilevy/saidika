package com.hackathon.saidika.repository;

import com.hackathon.saidika.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
}

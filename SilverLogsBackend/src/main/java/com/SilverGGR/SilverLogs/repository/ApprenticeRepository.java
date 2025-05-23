package com.SilverGGR.SilverLogs.repository;

import com.SilverGGR.SilverLogs.entity.Apprentice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprenticeRepository extends JpaRepository<Apprentice, Long> {

    Apprentice findByUsername(String username);
}

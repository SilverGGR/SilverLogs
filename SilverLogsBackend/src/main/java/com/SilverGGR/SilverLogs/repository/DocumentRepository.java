package com.SilverGGR.SilverLogs.repository;

import com.SilverGGR.SilverLogs.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByAuthUser_Username(String authUserUsername);

    Document findById(UUID id);

    void deleteById(UUID id);

}

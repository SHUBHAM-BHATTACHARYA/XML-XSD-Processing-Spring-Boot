package com.example.shubham.xmlService.xmlProcessing.repository;

import com.example.shubham.xmlService.xmlProcessing.entity.XmlPayloadRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XmlPayloadRepository extends JpaRepository<XmlPayloadRecord, Long> {
}

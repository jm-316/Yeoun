package com.yeoun.approval_new.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.approval_new.entity.ApprovalDocument;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

}

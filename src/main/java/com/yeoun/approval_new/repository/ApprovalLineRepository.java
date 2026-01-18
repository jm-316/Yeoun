package com.yeoun.approval_new.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.approval_new.entity.ApprovalLine;

public interface ApprovalLineRepository extends JpaRepository<ApprovalLine, Long>{

    Optional<ApprovalLine> findByApprovalDocumentApprovalIdAndApproverEmpId(
            Long approvalId, String approverId);

}

package com.yeoun.approval_new.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.approval_new.entity.ApprovalDocument;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long>,
													JpaSpecificationExecutor<ApprovalDocument>{
	
    // 내 기안
    Page<ApprovalDocument> findByEmployee_EmpId(String empId, Pageable pageable);
    
    // 결재 대기
    @Query("""
            SELECT DISTINCT ad 
            FROM ApprovalDocument ad
            JOIN ad.approvalLines al
            WHERE al.approver.empId = :approverId
              AND al.status = 'PENDING'
              AND NOT EXISTS (
                  SELECT al2 
                  FROM ApprovalLine al2
                  WHERE al2.approvalDocument = ad
                    AND al2.stepOrder < al.stepOrder
                    AND al2.status = 'PENDING'
              )
            """)
    Page<ApprovalDocument> findPendingByApproverId(@Param("approverId") String approverId, Pageable pageable);
    
    // 결재 완료
    @Query("""
            SELECT DISTINCT ad 
            FROM ApprovalDocument ad
            JOIN ad.approvalLines al
            WHERE al.approver.empId = :approverId
              AND al.status IN ('APPROVED', 'FINAL_APPROVED', 'REJECTED')
            """)
    Page<ApprovalDocument> findCompletedByApproverId(@Param("approverId") String approverId, Pageable pageable);

    // 상세 조회용 - 모든 관계 데이터 한 번에 가져오기
    @Query("SELECT ad FROM ApprovalDocument ad " +
           "LEFT JOIN FETCH ad.employee e " +
           "LEFT JOIN FETCH ad.approvalLeave " +
           "LEFT JOIN FETCH ad.approvalExpense " +
           "LEFT JOIN FETCH ad.approvalLines al " +
           "WHERE ad.approvalId = :approvalId")
    Optional<ApprovalDocument> findByIdWithAll(@Param("approvalId") Long approvalId);
}

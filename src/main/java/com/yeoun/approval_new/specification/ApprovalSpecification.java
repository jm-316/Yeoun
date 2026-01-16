package com.yeoun.approval_new.specification;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yeoun.approval_new.dto.ApprovalSearchDTO;
import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalLine;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class ApprovalSpecification {
    
    /**
     * 검색 조건으로 Specification 생성
     */
    public static Specification<ApprovalDocument> searchWith(ApprovalSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            String tab = searchDTO.getTab();
            String userId = searchDTO.getCurrentUserId();
            
            // ========== 탭별 조건 ==========
            if ("all".equals(tab)) {
                // 전체 결재: 내가 기안했거나 OR 결재선에 내가 있는 모든 문서
                Join<ApprovalDocument, ApprovalLine> lineJoin = root.join("approvalLines");
                
                Predicate myDraft = criteriaBuilder.equal(root.get("employee").get("empId"), userId);
                Predicate inApprovalLine = criteriaBuilder.equal(lineJoin.get("approver").get("empId"), userId);
                
                predicates.add(criteriaBuilder.or(myDraft, inApprovalLine));
                
            } else if ("my".equals(tab)) {
                // 내 기안: 내가 기안한 문서만
                predicates.add(criteriaBuilder.equal(root.get("employee").get("empId"), userId));
                
            } else if ("pending".equals(tab)) {
                // 결재 대기: 내가 기안한 건 제외, 결재선에 내가 있고 PENDING
                Join<ApprovalDocument, ApprovalLine> lineJoin = root.join("approvalLines");
                
                Predicate notMyDraft = criteriaBuilder.notEqual(root.get("employee").get("empId"), userId);
                Predicate inApprovalLine = criteriaBuilder.equal(lineJoin.get("approver").get("empId"), userId);
                Predicate isPending = criteriaBuilder.equal(lineJoin.get("status"), "PENDING");
                
                predicates.add(criteriaBuilder.and(notMyDraft, inApprovalLine, isPending));
                
            } else if ("completed".equals(tab)) {
                // 결재 완료: 내가 기안한 건 제외, 결재선에 내가 있고 승인/반려됨
                Join<ApprovalDocument, ApprovalLine> lineJoin = root.join("approvalLines");
                
                Predicate notMyDraft = criteriaBuilder.notEqual(root.get("employee").get("empId"), userId);
                Predicate inApprovalLine = criteriaBuilder.equal(lineJoin.get("approver").get("empId"), userId);
                Predicate isCompleted = lineJoin.get("status").in("APPROVED", "FINAL_APPROVED", "REJECTED");
                
                predicates.add(criteriaBuilder.and(notMyDraft, inApprovalLine, isCompleted));
            }
            
            // ========== 날짜 범위 검색 ==========
            if (searchDTO.getSearchStartDate() != null && !searchDTO.getSearchStartDate().isEmpty()) {
                LocalDate startDate = LocalDate.parse(searchDTO.getSearchStartDate());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate));
            }
            
            if (searchDTO.getSearchEndDate() != null && !searchDTO.getSearchEndDate().isEmpty()) {
                LocalDate endDate = LocalDate.parse(searchDTO.getSearchEndDate());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate));
            }
            
            // ========== 키워드 검색 (기안자명 OR 문서제목) ==========
            if (searchDTO.getSearchKeyword() != null && !searchDTO.getSearchKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getSearchKeyword() + "%";
                
                Predicate empNameLike = criteriaBuilder.like(root.get("employee").get("empName"), keyword);
                Predicate titleLike = criteriaBuilder.like(root.get("approvalTitle"), keyword);
                
                predicates.add(criteriaBuilder.or(empNameLike, titleLike));
            }
            
            // DISTINCT 처리 (JOIN으로 인한 중복 제거)
            query.distinct(true);
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

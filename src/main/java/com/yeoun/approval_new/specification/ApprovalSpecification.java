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
            
            // 1. 탭별 조건
            String tab = searchDTO.getTab();
            String userId = searchDTO.getCurrentUserId();
            
            if ("my".equals(tab)) {
                // 내 기안
                predicates.add(criteriaBuilder.equal(root.get("employee").get("empId"), userId));
                
            } else if ("pending".equals(tab)) {
                // 결재 대기
                Join<ApprovalDocument, ApprovalLine> lineJoin = root.join("approvalLines");
                predicates.add(criteriaBuilder.equal(lineJoin.get("approver").get("empId"), userId)); 
                predicates.add(criteriaBuilder.equal(lineJoin.get("status"), "PENDING"));
                
            } else if ("completed".equals(tab)) {
                // 결재 완료
                Join<ApprovalDocument, ApprovalLine> lineJoin = root.join("approvalLines");
                predicates.add(criteriaBuilder.equal(lineJoin.get("approver").get("empId"), userId)); 
                predicates.add(lineJoin.get("status").in("APPROVED", "FINAL_APPROVED", "REJECTED"));
            }
            // "all"은 조건 없음
            
            // 2. 날짜 범위 검색
            if (searchDTO.getSearchStartDate() != null && !searchDTO.getSearchStartDate().isEmpty()) {
                LocalDate startDate = LocalDate.parse(searchDTO.getSearchStartDate());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate));
            }
            
            if (searchDTO.getSearchEndDate() != null && !searchDTO.getSearchEndDate().isEmpty()) {
                LocalDate endDate = LocalDate.parse(searchDTO.getSearchEndDate());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate));
            }
            
            // 3. 키워드 검색 (기안자명 OR 문서제목)
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

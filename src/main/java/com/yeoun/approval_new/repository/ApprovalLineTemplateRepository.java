package com.yeoun.approval_new.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.approval_new.entity.ApprovalLineTemplate;

public interface ApprovalLineTemplateRepository extends JpaRepository<ApprovalLineTemplate, Long>{
	
	// 결재선 템플릿 조회
	List<ApprovalLineTemplate> findByEmp_EmpId(String empId);
	
	// 결재선템플릿ID로 결재선 템플릿 조회
	Optional<ApprovalLineTemplate> findByTemplateIdAndEmp_EmpId(Long templateId, String empId);
	
	// 기본값으로 설정된 결재선템플릿 조회
	Optional<ApprovalLineTemplate> findByEmp_EmpIdAndIsDefault(String empId, String isDefault);
}

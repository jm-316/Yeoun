package com.yeoun.approval_new.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.approval_new.dto.ApprovalLineTemplateCreateRequestDTO;
import com.yeoun.approval_new.dto.ApprovalLineTemplateResponseDTO;
import com.yeoun.approval_new.entity.ApprovalLineTemplate;
import com.yeoun.approval_new.entity.ApprovalLineTemplateDetail;
import com.yeoun.approval_new.repository.ApprovalLineTemplateRepository;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ApprovalLineTemplateService {

    private final ApprovalLineTemplateRepository approvalLineTemplateRepository;
    private final EmpRepository empRepository;
    
    // 결재선 목록 조회
	public List<ApprovalLineTemplateResponseDTO> getMyTemplates(String empId) {
	    List<ApprovalLineTemplate> templates = approvalLineTemplateRepository.findByEmp_EmpId(empId);
	    
	    return templates.stream()
	        .map(ApprovalLineTemplateResponseDTO::fromEntity)
	        .collect(Collectors.toList());
	}
    
    
    // 결재선 템플릿 저장
	public ApprovalLineTemplateResponseDTO createTemplate(ApprovalLineTemplateCreateRequestDTO request, String loginEmpId) {
        // 입력 값 검수
        if (request.getTemplateName() == null || request.getTemplateName().trim().isEmpty()) {
            throw new IllegalArgumentException("템플릿 이름을 입력하세요.");
        }

        if (request.getApprovers() == null || request.getApprovers().isEmpty()) {
            throw new IllegalArgumentException("최소 1명 이상의 결재자를 선택하세요.");
        }

        if (request.getApprovers().size() > 3) {
            throw new IllegalArgumentException("결재선은 최대 3명까지만 추가할 수 있습니다.");
        }
        
        // 로그인 사용자 조회
        Emp loginUser = empRepository.findByEmpId(loginEmpId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
		
        // 결재선 템플릿 생성
        ApprovalLineTemplate template = ApprovalLineTemplate.builder()
                .templateName(request.getTemplateName().trim())
                .emp(loginUser)
                .isDefault("N")
                .build();
        
        // 결재선 템플릿 디테일 정보 생성
        List<ApprovalLineTemplateDetail> details = request.getApprovers()
                .stream()
                .map(approverInfo -> {
                    Emp approver = empRepository.findByEmpId(approverInfo.getEmpId())
                        .orElseThrow(() -> new IllegalArgumentException(
                            "결재자 정보를 찾을 수 없습니다: " + approverInfo.getEmpId()));

                    return ApprovalLineTemplateDetail.builder()
                        .template(template)
                        .approver(approver)
                        .stepOrder(approverInfo.getStepOrder())
                        .build();
                })
                .collect(Collectors.toList());
        
        template.setDetails(details);
        // 템플릿 저장
        ApprovalLineTemplate savedTemplate = approvalLineTemplateRepository.save(template);
        
        
        log.info("결재선 템플릿 저장 완료: {} (ID: {})", savedTemplate.getTemplateName(), savedTemplate.getTemplateId());
        return ApprovalLineTemplateResponseDTO.fromEntity(savedTemplate);
	}
	
	// 결재선 기본값 설정
	@Transactional
	public void setAsDefault(Long templateId, String loginEmpId) {
	    // 해당 템플릿이 로그인 사용자의 것인지 확인
	    ApprovalLineTemplate template = approvalLineTemplateRepository.findByTemplateIdAndEmp_EmpId(templateId, loginEmpId)
	        .orElseThrow(() -> new IllegalArgumentException("해당 결재선을 찾을 수 없습니다."));
	    
	    // 기존 기본값 설정 해제
	    List<ApprovalLineTemplate> userTemplates = approvalLineTemplateRepository.findByEmp_EmpId(loginEmpId);
	    userTemplates.forEach(t -> t.setIsDefault("N"));
	    
	    approvalLineTemplateRepository.saveAll(userTemplates);
	    
	    //선택한 템플릿을 기본값으로 설정
	    template.setIsDefault("Y");
	    approvalLineTemplateRepository.save(template);
	    
	    log.info("결재선 템플릿 기본값 설정: {} (ID: {})", template.getTemplateName(), templateId);
	}

	// 결재선 템플릿 삭제
	@Transactional
	public void deleteTemplate(Long templateId, String loginEmpId) {
	    // 해당 템플릿이 로그인 사용자의 것인지 확인
	    ApprovalLineTemplate template = approvalLineTemplateRepository.findByTemplateIdAndEmp_EmpId(templateId, loginEmpId)
	        .orElseThrow(() -> new IllegalArgumentException("해당 결재선을 찾을 수 없습니다."));
	    
	    // 삭제
	    approvalLineTemplateRepository.delete(template);
	    
	    log.info("결재선 템플릿 삭제 완료: {} (ID: {})", template.getTemplateName(), templateId);
	}

	// 기본값으로 설정된 결재선템플릿 불러오기
	@Transactional(readOnly = true)
	public ApprovalLineTemplateResponseDTO getDefaultTemplate(String empId) {
	    return approvalLineTemplateRepository.findByEmp_EmpIdAndIsDefault(empId, "Y")
	        .map(ApprovalLineTemplateResponseDTO::fromEntity)
	        .orElse(null);
	}

}

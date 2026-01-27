package com.yeoun.approval_new.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.approval_new.dto.ApprovalLineTemplateCreateRequestDTO;
import com.yeoun.approval_new.dto.ApprovalLineTemplateResponseDTO;
import com.yeoun.approval_new.service.ApprovalLineTemplateService;
import com.yeoun.auth.dto.LoginDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/manage/approval")
@Log4j2
@RequiredArgsConstructor
public class ApprovalManageController {
	
    private final ApprovalLineTemplateService approvalLineTemplateService;
	
    // 내 결재선 템플릿 목록 조회
    @GetMapping("/line-template/my")
    public ResponseEntity<?> getMyApprovalLineTemplates(
            @AuthenticationPrincipal LoginDTO loginDTO) {
        
        try {
            log.info("내 결재선 템플릿 목록 조회: {}", loginDTO.getEmpId());
            
            List<ApprovalLineTemplateResponseDTO> response = 
                approvalLineTemplateService.getMyTemplates(loginDTO.getEmpId());
            log.info("내 결재선 템플릿 목록 조회: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("결재선 템플릿 조회 중 에러:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("조회 중 오류가 발생했습니다.");
        }
    }
	
    // 결재선템플릿 저장
	@PostMapping("/line-template")
    public ResponseEntity<?> createApprovalLineTemplate(
            @RequestBody ApprovalLineTemplateCreateRequestDTO request,
            @AuthenticationPrincipal LoginDTO loginDTO) {
        try {
            log.info("결재선 템플릿 저장 요청: {}", request.getTemplateName());

            ApprovalLineTemplateResponseDTO response = 
                approvalLineTemplateService.createTemplate(request, loginDTO.getEmpId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("유효성 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        } catch (Exception e) {
            log.error("결재선 템플릿 저장 중 에러:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("저장 중 오류가 발생했습니다.");
        }
	}
	
	// 결재선 템플릿 기본값 설정
	@PatchMapping("/line-template/{templateId}/default")
	public ResponseEntity<?> setDefaultTemplate(
	        @PathVariable("templateId") Long templateId,
	        @AuthenticationPrincipal LoginDTO loginDTO) {
	    
	    try {
	        log.info("결재선 템플릿 기본값 설정: {}", templateId);
	        
	        approvalLineTemplateService.setAsDefault(templateId, loginDTO.getEmpId());
	        
	        return ResponseEntity.ok("기본값이 설정되었습니다.");
	        
	    } catch (IllegalArgumentException e) {
	        log.error("유효성 검증 실패: {}", e.getMessage());
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        log.error("기본값 설정 중 에러:", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("처리 중 오류가 발생했습니다.");
	    }
	}
	
	// 결재선 템플릿 삭제
	@DeleteMapping("/line-template/{templateId}")
	public ResponseEntity<?> deleteTemplate(
	        @PathVariable("templateId") Long templateId,
	        @AuthenticationPrincipal LoginDTO loginDTO) {
	    
	    try {
	        log.info("결재선 템플릿 삭제: {}", templateId);
	        
	        approvalLineTemplateService.deleteTemplate(templateId, loginDTO.getEmpId());
	        
	        return ResponseEntity.ok("결재선이 삭제되었습니다.");
	        
	    } catch (IllegalArgumentException e) {
	        log.error("유효성 검증 실패: {}", e.getMessage());
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        log.error("템플릿 삭제 중 에러:", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("처리 중 오류가 발생했습니다.");
	    }
	}
	
	// 기본값으로 설정된 결재선 템플릿 불러오기
	@GetMapping("/line-template/default")
	public ResponseEntity<?> getDefaultApprovalLineTemplate(
	        @AuthenticationPrincipal LoginDTO loginDTO) {
	    
	    try {
	        log.info("기본값 결재선 템플릿 조회: {}", loginDTO.getEmpId());
	        
	        ApprovalLineTemplateResponseDTO response = 
	            approvalLineTemplateService.getDefaultTemplate(loginDTO.getEmpId());
	        
	        if (response == null) {
	            return ResponseEntity.noContent().build();  // 기본값이 없으면 204 No Content
	        }
	        
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        log.error("기본값 템플릿 조회 중 에러:", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("조회 중 오류가 발생했습니다.");
	    }
	}
}

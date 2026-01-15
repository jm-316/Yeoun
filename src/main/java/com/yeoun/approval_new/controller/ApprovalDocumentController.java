package com.yeoun.approval_new.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoun.approval.dto.ApprovalDocGridDTO;
import com.yeoun.approval_new.dto.ApprovalCreateRequestDTO;
import com.yeoun.approval_new.dto.ApprovalDocumentDTO;
import com.yeoun.approval_new.dto.ApprovalExpenseDTO;
import com.yeoun.approval_new.dto.ApprovalLeaveDTO;
import com.yeoun.approval_new.dto.ApprovalLineDTO;
import com.yeoun.approval_new.service.ApprovalDocumentService;
import com.yeoun.auth.dto.LoginDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/new/approval")
@RequiredArgsConstructor
@Log4j2
public class ApprovalDocumentController {
	 private final ObjectMapper objectMapper;
	 private final ApprovalDocumentService adService;
	
	//전자결재 연결페이지
  	@GetMapping("/approval_doc")
  	public String approvalDoc(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		return "approval/approval_doc";
 	}
  	
  	@PostMapping("/create")
  	@ResponseBody
  	public ResponseEntity<Map<String, Object>> createApprovalDocument(
	    @RequestPart("document") ApprovalDocumentDTO document,
	    @RequestPart(value = "leave", required = false) ApprovalLeaveDTO leave,
	    @RequestPart(value = "expense", required = false) ApprovalExpenseDTO expense,
	    @RequestPart("approvers") List<ApprovalLineDTO> approvers,
	    @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments,
  	    BindingResult bindingResult
  	) {
        Map<String, Object> result = new HashMap<>();
        
        // Validation 에러 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            
            result.put("success", false);
            result.put("message", errorMessage);
            return ResponseEntity.badRequest().body(result);
        }
        
  	    try {
  	    	Long approvalId = adService.createDocument(document, leave, expense, approvers, attachments);
  	        
  	        result.put("success", true);
  	        result.put("message", "결재 문서가 등록되었습니다.");
  	        result.put("approvalId", approvalId);
  	        
  	        log.info("결재 문서 등록 성공: approvalId={}", approvalId);
  	    	
  	    } catch (IllegalArgumentException e) {
  	        log.error("유효성 검증 실패: {}", e.getMessage());
  	        result.put("success", false);
  	        result.put("message", e.getMessage());
  	        return ResponseEntity.badRequest().body(result);
  	        
  	    } catch (Exception e) {
  	        log.error("결재 문서 등록 실패", e);
  	        result.put("success", false);
  	        result.put("message", "등록 중 오류가 발생했습니다: " + e.getMessage());
  	        return ResponseEntity.internalServerError().body(result);
  	    }
  	    
  	    return ResponseEntity.ok(result);
  	}
}

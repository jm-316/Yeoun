package com.yeoun.approval_new.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoun.approval.dto.ApprovalDocGridDTO;
import com.yeoun.approval_new.dto.ApprovalResponseDTO;
import com.yeoun.approval_new.dto.ApprovalSearchDTO;
import com.yeoun.approval_new.dto.ApprovalDocumentDTO;
import com.yeoun.approval_new.dto.ApprovalExpenseDTO;
import com.yeoun.approval_new.dto.ApprovalLeaveDTO;
import com.yeoun.approval_new.dto.ApprovalLineDTO;
import com.yeoun.approval_new.dto.ApprovalListDTO;
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
  	
  	//결재 등록
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
  	
    // 결재 문서 목록 조회
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApprovalList(
	    // 탭
	    @RequestParam(name = "tab", defaultValue = "all") String tab,
	    
	    // 페이지네이션
	    @RequestParam(name = "page", defaultValue = "1") int page,
	    @RequestParam(name = "perPage", defaultValue = "20") int perPage,
	    
	    // 정렬 (Toast UI Grid가 보냄)
	    @RequestParam(name = "sortColumn", required = false) String sortColumn,
	    @RequestParam(name = "sortAscending", required = false) Boolean sortAscending,
	    
	    // 상단 검색 조건
	    @RequestParam(name = "searchStartDate", required = false) String searchStartDate,
	    @RequestParam(name = "searchEndDate", required = false) String searchEndDate,
	    @RequestParam(name = "searchKeyword", required = false) String searchKeyword,
	    
	    // Grid 필터 (Toast UI Grid가 보냄)
	    @RequestParam(required = false) Map<String, String> allParams,  // 모든 파라미터
	    
        @AuthenticationPrincipal LoginDTO loginDTO
    ) {
        
        log.info("결재 목록 조회 요청: tab={}, page={}, perPage={}, userId={}", 
            tab, page, perPage, loginDTO.getEmpId());
        
        try {
            String currentUserId = loginDTO.getEmpId();
            
            // 검색 조건 DTO 생성
            ApprovalSearchDTO searchDTO = ApprovalSearchDTO.builder()
                .tab(tab)
                .currentUserId(currentUserId)
                .searchStartDate(searchStartDate)
                .searchEndDate(searchEndDate)
                .searchKeyword(searchKeyword)
                .sortColumn(sortColumn)
                .sortAscending(sortAscending)
                .build();
            log.info("searchDTO : " + searchDTO);
            
            // 서비스 호출
            Page<ApprovalListDTO> pageData = adService.getApprovalList(
            	searchDTO,
                page, 
                perPage
            );
            
            log.info("결재 목록 조회 성공: tab={}, totalCount={}", tab, pageData.getTotalElements());
            
            // Toast UI Grid 응답 형식
            Map<String, Object> data = Map.of(
                "contents", pageData.getContent(),
                "pagination", Map.of(
                    "page", page,
                    "totalCount", pageData.getTotalElements()
                )
            );
//            
            Map<String, Object> response = Map.of(
                "result", true,
                "data", data
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("결재 목록 조회 실패: tab={}, page={}", tab, page, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("message", "목록 조회에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

	// 결재 문서 상세 조회
    @GetMapping("/detail/{approvalId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApprovalDetail(
        @PathVariable(name = "approvalId") Long approvalId,
        @AuthenticationPrincipal LoginDTO loginDTO
    ) {
        
        log.info("결재 문서 상세 조회: approvalId={}, userId={}", approvalId, loginDTO.getEmpId());
        
        try {
            // 서비스 호출
            ApprovalResponseDTO detail = adService.getApprovalDetail(approvalId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", true);
            response.put("data", detail);
            
            log.info("결재 문서 상세 조회 성공: approvalId={}", approvalId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("결재 문서 상세 조회 실패 - 문서 없음: approvalId={}", approvalId);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("message", "존재하지 않는 문서입니다.");
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("결재 문서 상세 조회 실패: approvalId={}", approvalId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("message", "문서 조회에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
	// 승인 처리
    @PostMapping("/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveDocument(
        @RequestBody Map<String, Object> request,
        @AuthenticationPrincipal LoginDTO loginDTO
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long approvalId = Long.parseLong(request.get("approvalId").toString());
            Boolean isFinalApproval = (Boolean) request.get("isFinalApproval");
            String currentUserId = loginDTO.getEmpId();
            
            log.info("승인 처리 요청: approvalId={}, isFinal={}, userId={}", 
                approvalId, isFinalApproval, currentUserId);
            
            adService.approveDocument(approvalId, currentUserId, isFinalApproval);
            
            result.put("result", true);
            result.put("message", isFinalApproval ? "전결 승인되었습니다." : "승인되었습니다.");
            
            log.info("승인 처리 완료: approvalId={}", approvalId);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.error("승인 처리 실패 - 권한 없음: {}", e.getMessage());
            result.put("result", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("승인 처리 실패", e);
            result.put("result", false);
            result.put("message", "승인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // 반려 처리
    @PostMapping("/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectDocument(
        @RequestBody Map<String, Object> request,
        @AuthenticationPrincipal LoginDTO loginDTO
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long approvalId = Long.parseLong(request.get("approvalId").toString());
            String rejectReason = request.get("rejectReason").toString();
            String currentUserId = loginDTO.getEmpId();
            
            log.info("반려 처리 요청: approvalId={}, userId={}", approvalId, currentUserId);
            
            // 서비스 호출
            adService.rejectDocument(approvalId, currentUserId, rejectReason);
            
            result.put("result", true);
            result.put("message", "반려되었습니다.");
            
            log.info("반려 처리 완료: approvalId={}", approvalId);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.error("반려 처리 실패 - 권한 없음: {}", e.getMessage());
            result.put("result", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("반려 처리 실패", e);
            result.put("result", false);
            result.put("message", "반려 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    //휴가 중복 체크
    @GetMapping("/check-leave-duplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLeaveDuplicate(
		@AuthenticationPrincipal LoginDTO loginDTO,
		@RequestParam(name = "leaveType") String leaveType,
	    @RequestParam(name = "startDate") String startDate,
	    @RequestParam(name = "endDate") String endDate    		
    ) {
    	String empId = loginDTO.getEmpId();
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> result = adService.checkLeaveDuplicate(empId, start, end, leaveType);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("휴가 중복 체크 실패", e);
            return ResponseEntity.ok(Map.of("isDuplicate", false));
        }
    }

}

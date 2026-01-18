package com.yeoun.approval_new.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.approval_new.dto.ApprovalDocumentDTO;
import com.yeoun.approval_new.dto.ApprovalExpenseDTO;
import com.yeoun.approval_new.dto.ApprovalLeaveDTO;
import com.yeoun.approval_new.dto.ApprovalLineDTO;
import com.yeoun.approval_new.dto.ApprovalListDTO;
import com.yeoun.approval_new.dto.ApprovalResponseDTO;
import com.yeoun.approval_new.dto.ApprovalSearchDTO;
import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalExpense;
import com.yeoun.approval_new.entity.ApprovalLeave;
import com.yeoun.approval_new.entity.ApprovalLine;
import com.yeoun.approval_new.repository.ApprovalDocumentRepository;
import com.yeoun.approval_new.repository.ApprovalExpenseRepository;
import com.yeoun.approval_new.repository.ApprovalLeaveRepository;
import com.yeoun.approval_new.repository.ApprovalLineRepository;
import com.yeoun.approval_new.specification.ApprovalSpecification;
import com.yeoun.common.dto.AlarmDTO;
import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.service.AlarmService;
import com.yeoun.common.util.FileUtil;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.leave.service.LeaveService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApprovalDocumentService {
	private final ApprovalDocumentRepository documentRepository;
	private final ApprovalExpenseRepository expenseRepository;
	private final ApprovalLeaveRepository leaveRepository;
	private final ApprovalLineRepository lineRepository;
	private final EmpRepository empRepository;
	private final FileAttachRepository fileAttachRepository;
	
	private final AlarmService alarmService;
	private final LeaveService leaveService;
	
	private final FileUtil fileUtil;
	
	// 문서등록 서비스
	@Transactional
	public Long createDocument(
		ApprovalDocumentDTO documentDTO
		, ApprovalLeaveDTO leaveDTO
		, ApprovalExpenseDTO expenseDTO
		, List<ApprovalLineDTO> approverDTOs
		, List<MultipartFile> attachments) throws IOException {
		
		// ApprovalDocument 생성
		ApprovalDocument document = documentDTO.toEntity(empRepository);
		log.info("Document : " + document);
        log.info("결재 문서 엔티티 생성: formType={}, title={}", 
                document.getFormType(), document.getApprovalTitle());
        // 휴가
        if ("leave".equals(document.getFormType()) && leaveDTO != null) {
            ApprovalLeave leave = leaveDTO.toEntity(document);
            document.setApprovalLeave(leave);
            log.info("휴가 정보 추가: type={}, start={}, end={}, days={}", 
                leave.getLeaveType(), leave.getLeaveStartDate(), 
                leave.getLeaveEndDate(), leave.getLeaveDays());
        }
        
        // 지출
        if ("expense".equals(document.getFormType()) && expenseDTO != null) {
            ApprovalExpense expense = expenseDTO.toEntity(document);
            document.setApprovalExpense(expense);
            log.info("지출 정보 추가: type={}, vendor={}, amount={}", 
                expense.getExpenseType(), expense.getExpenseVendor(), expense.getExpenseAmount());
        }
        
        // 결재선
        if (approverDTOs != null && !approverDTOs.isEmpty()) {
        	log.info("결재자 목록 : " + approverDTOs);
            List<ApprovalLine> lines = approverDTOs.stream()
                .map(dto -> dto.toEntity(document, empRepository))
                .collect(Collectors.toList());
            document.setApprovalLines(lines);
            log.info("결재선 추가: approverCount={}", lines.size());
        }
        
        // 문서 저장
        ApprovalDocument savedDocument = documentRepository.save(document);
        
        log.info("결재 문서 저장 완료: approvalId={}", savedDocument.getApprovalId());
        
        // 첨부파일 저장
        if (attachments != null && !attachments.isEmpty()) {
        	List<FileAttach> fileList = fileUtil.uploadFile(document, attachments).stream()
        			.map(FileAttachDTO::toEntity)
        			
        			.toList();
        	// 파일 DB 저장
        	fileAttachRepository.saveAll(fileList);
        }
		
		return savedDocument.getApprovalId();
	}
	
	// 결재문서 조회 로직
	@Transactional(readOnly = true)
	public Page<ApprovalListDTO> getApprovalList(ApprovalSearchDTO searchDTO, int page, int perPage) {
	    // 정렬
	    Sort sort = createSort(searchDTO.getSortColumn(), searchDTO.getSortAscending());
	    Pageable pageable = PageRequest.of(page - 1, perPage, sort);
		
	    // 동적 쿼리 실행
	    Specification<ApprovalDocument> spec = ApprovalSpecification.searchWith(searchDTO);
	    Page<ApprovalDocument> documentPage = documentRepository.findAll(spec, pageable);
	    
	    log.info("결재 목록 조회 완료: tab={}, totalCount={}", searchDTO.getTab(), documentPage.getTotalElements());
	    
	    // Entity → DTO 변환
	    return documentPage.map(ApprovalListDTO::fromEntity);
	}
	
	// 결재 문서 상세 조회
	@Transactional(readOnly = true)
	public ApprovalResponseDTO getApprovalDetail(Long approvalId) {
	    
	    // 문서 조회 (모든 관계 fetch join)
	    ApprovalDocument document = documentRepository.findByIdWithAll(approvalId)
	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다: " + approvalId));
	    
	    log.info("문서 조회 완료: approvalId={}, formType={}", approvalId, document.getFormType());
	    
	    // Entity → DTO 변환
	    ApprovalResponseDTO responseDTO = ApprovalResponseDTO.fromEntity(
	        document, 
	        empRepository,
	        fileAttachRepository
	    );
	    
	    log.info("첨부파일 개수: {}", 
	            responseDTO.getAttachments() != null ? responseDTO.getAttachments().size() : 0);
	    
	    return responseDTO;
	}
	
    // 결재 승인 로직
	@Transactional
	public void approveDocument(Long approvalId, String currentUserId, Boolean isFinalApproval) {
		// 문서조회
		ApprovalDocument document = documentRepository.findByIdWithAll(approvalId)
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문서입니다: " + approvalId));
		
		// 결재 양식
		String formType = document.getFormType();
		// 결재 상태
		String status = document.getStatus();
		
		// 현재 결재권 엔티티 정보
	    ApprovalLine currentApprover = lineRepository
	            .findByApprovalDocumentApprovalIdAndApproverEmpId(approvalId, currentUserId)
	            .orElseThrow(() -> new EntityNotFoundException("현재 결재자 정보를 찾을 수 없습니다."));
	    
	    // 권한 검증 (이미 처리했거나 차례가 아니면 에러)
	    if (!"PENDING".equals(currentApprover.getStatus())) {
	        throw new IllegalArgumentException("이미 처리된 결재입니다.");
	    }
	    
	    // 결재선 목록
	    List<ApprovalLine> approvers = document.getApprovalLines();
	    int totalApprovers = approvers.size();
	    int currentStep = currentApprover.getStepOrder();
	    
	    log.info("승인 처리 시작: approvalId={}, step={}/{}, isFinal={}", 
	        approvalId, currentStep, totalApprovers, isFinalApproval);
	    
	    // ========== 전결 승인인 경우 ==========
	    if (isFinalApproval != null && isFinalApproval) {
	        // 전결 승인으로 상태 변경
	        currentApprover.setStatus("FINAL_APPROVED");
	        currentApprover.setApprovedAt(LocalDateTime.now());
	        
	        // 이후 결재선 모두 SKIPPED 처리
	        approvers.stream()
	            .filter(line -> line.getStepOrder() > currentStep)
	            .forEach(line -> {
	                line.setStatus("SKIPPED");
	                line.setApprovedAt(LocalDateTime.now());
	                log.info("결재선 건너뜀: step={}, approverId={}", line.getStepOrder(), line.getApprover().getEmpId());
	            });
	        
	        // 문서 상태 최종 승인
	        document.setStatus("APPROVED");
	        
	        log.info("전결 승인 처리: 이후 {}개 결재선 건너뜀", totalApprovers - currentStep);
	        
	        // 후처리 호출
	        processAfterFinalApproval(document);
	        
	        // 기안자에게 알림
	        String message = "기안하신 결재 문서가 " + currentStep + "차에서 전결 승인되었습니다.";
	        sendAlarmToDrafter(document, message);
	        
	        return;
	    }
	    
	    // ========== 일반 승인인 경우 ==========
	    currentApprover.setStatus("APPROVED");
	    currentApprover.setApprovedAt(LocalDateTime.now());
	    
	    // 마지막 결재자인지 확인
	    if (currentStep == totalApprovers) {
	        // ========== 최종 승인 (마지막 결재자) ==========
	        document.setStatus("APPROVED");
	        
	        log.info("최종 승인 완료: approvalId={}", approvalId);
	        
	        // 후처리 호출
	        processAfterFinalApproval(document);
	        
	        // 기안자에게 알림
	        String message = "기안하신 결재 문서가 최종 승인되었습니다.";
	        sendAlarmToDrafter(document, message);
	        
	    } else {
	        // ========== 중간 승인 ==========
	        log.info("중간 승인 완료: approvalId={}, 다음 결재자: {}차", approvalId, currentStep + 1);
	        
	        // 기안자에게 알림
	        String message = "기안하신 결재 문서의 " + currentStep + "차 승인이 완료되었습니다.";
	        sendAlarmToDrafter(document, message);
	        
	        // 다음 결재자에게 알림 (선택사항)
	        ApprovalLine nextApprover = approvers.stream()
	            .filter(line -> line.getStepOrder() == currentStep + 1)
	            .findFirst()
	            .orElse(null);
	        
	        if (nextApprover != null) {
	            String nextMessage = "새로운 결재 문서가 도착했습니다.";
	            sendAlarmToApprover(nextApprover.getApprover().getEmpId(), nextMessage);
	        }
	    }
	}

	// 결재 반려 로직
	@Transactional
	public void rejectDocument(Long approvalId, String currentUserId, String rejectReason) {
	    
	    ApprovalDocument document = documentRepository.findByIdWithAll(approvalId)
	        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문서입니다: " + approvalId));
	    
	    ApprovalLine currentApprover = lineRepository
	        .findByApprovalDocumentApprovalIdAndApproverEmpId(approvalId, currentUserId)
	        .orElseThrow(() -> new EntityNotFoundException("현재 결재자 정보를 찾을 수 없습니다."));
	    
	    if (!"PENDING".equals(currentApprover.getStatus())) {
	        throw new IllegalArgumentException("이미 처리된 결재입니다.");
	    }
	    
	    List<ApprovalLine> approvers = document.getApprovalLines();
	    int currentStep = currentApprover.getStepOrder();
	    
	    // 반려 처리
	    currentApprover.setStatus("REJECTED");
	    currentApprover.setApprovedAt(LocalDateTime.now());
	    document.setRejectReason(rejectReason);
	    
	    // ========== 후순위 결재자 CANCELLED 처리 ==========
	    approvers.stream()
	        .filter(line -> line.getStepOrder() > currentStep)
	        .forEach(line -> {
	            line.setStatus("CANCELLED");
	            line.setApprovedAt(LocalDateTime.now());
	            log.info("결재선 취소됨: step={}, approverId={}", 
	                line.getStepOrder(), line.getApprover().getEmpId());
	        });
	    
	    // 문서 상태 반려
	    document.setStatus("REJECTED");
	    
	    log.info("반려 처리 완료: approvalId={}, step={}, reason={}", 
	        approvalId, currentApprover.getStepOrder(), rejectReason);
	    
	    // 기안자에게 알림
	    String message = "기안하신 결재 문서가 " + currentApprover.getStepOrder() + "차에서 반려되었습니다.\n사유: " + rejectReason;
	    sendAlarmToDrafter(document, message);
	}
	
	
	
	// -----------------------------------------------------------------------------
	// 유틸 모음
	// sort생성
    private Sort createSort(String sortColumn, Boolean sortAscending) {
        // 기본 정렬: 생성일시 내림차순
        if (sortColumn == null || sortColumn.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        // 정렬 방향 결정
        Sort.Direction direction = (sortAscending != null && sortAscending) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        // 컬럼명 매핑 (Grid 컬럼명 → Entity 필드명)
        String entityField = mapColumnToEntityField(sortColumn);
        
        return Sort.by(direction, entityField);
    }
    
    // 컬럼명과 엔티티 필드 매칭
    private String mapColumnToEntityField(String columnName) {
        switch (columnName) {
            case "approvalId":
                return "approvalId";
            case "formType":
                return "formType";
            case "approvalTitle":
                return "approvalTitle";
            case "empName":
                return "employee.empName";  // 조인 필드
            case "createdDate":
                return "createdDate";
            case "finishDate":
                return "finishDate";
            case "status":
                return "status";
            default:
                return "createdAt";  // 기본값
        }
    }
    
	// 최종 승인 후처리
    private void processAfterFinalApproval(ApprovalDocument document) {
        String formType = document.getFormType();
        
        log.info("후처리 시작: formType={}", formType);
        
        switch (formType) {
            case "free":
                log.info("자유양식 - 후처리 없음");
                break;
                
            case "leave":
            	log.info("휴가양식 - leaveService - createAnnualLeave 호출 ");
            	leaveService.createAnnual(document);
            	
                break;
                
            case "expense":
            	log.info("지출양식 - 후처리 없음");
                break;
                
            default:
                log.warn("알 수 없는 양식 타입: {}", formType);
        }
    }
    
	// 기안자에게 알림
    private void sendAlarmToDrafter(ApprovalDocument document, String message) {
        AlarmDTO alarmDTO = AlarmDTO.builder()
            .empId(document.getEmployee().getEmpId())
            .alarmMessage(message)
            .alarmStatus("N")
            .alarmLink("/new/approval/approval_doc")
            .build();
        
        alarmService.sendPersonalMessage(alarmDTO);
    }
    
	// 결재자에게 알림
    private void sendAlarmToApprover(String approverId, String message) {
        AlarmDTO alarmDTO = AlarmDTO.builder()
            .empId(approverId)
            .alarmMessage(message)
            .alarmStatus("N")
            .alarmLink("/new/approval/approval_doc")
            .build();
        
        alarmService.sendPersonalMessage(alarmDTO);
    }

}

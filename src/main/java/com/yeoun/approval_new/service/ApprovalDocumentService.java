package com.yeoun.approval_new.service;

import java.io.IOException;
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
import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.util.FileUtil;
import com.yeoun.emp.repository.EmpRepository;

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

}

package com.yeoun.approval_new.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.approval_new.dto.ApprovalDocumentDTO;
import com.yeoun.approval_new.dto.ApprovalExpenseDTO;
import com.yeoun.approval_new.dto.ApprovalLeaveDTO;
import com.yeoun.approval_new.dto.ApprovalLineDTO;
import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalExpense;
import com.yeoun.approval_new.entity.ApprovalLeave;
import com.yeoun.approval_new.entity.ApprovalLine;
import com.yeoun.approval_new.repository.ApprovalDocumentRepository;
import com.yeoun.approval_new.repository.ApprovalExpenseRepository;
import com.yeoun.approval_new.repository.ApprovalLeaveRepository;
import com.yeoun.approval_new.repository.ApprovalLineRepository;
import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.util.FileUtil;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.transaction.Transactional;
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
}

package com.yeoun.approval_new.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalLine;
import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApprovalResponseDTO {
    
    // ========== 기본 문서 정보 (ApprovalDocumentDTO 역할) ==========
    private ApprovalDocumentDTO document;
    
    // ========== 양식별 정보 ==========
    private ApprovalLeaveDTO leave;      // 휴가 (nullable)
    private ApprovalExpenseDTO expense;  // 지출 (nullable)
    
    // ========== 결재선 정보 ==========
    private List<ApprovalLineDTO> approvers;
    
    // ========== 첨부파일 정보 ==========
    private List<FileAttachDTO> attachments;
    
    
    private static ModelMapper modelMapper = new ModelMapper();
    /**
     * Entity → DTO (상세 조회용)
     */
    public static ApprovalResponseDTO fromEntity(
        ApprovalDocument entity, 
        EmpRepository empRepository,
        FileAttachRepository fileAttachRepository
    ) {
        // 기본 문서
        ApprovalDocumentDTO documentDTO = ApprovalDocumentDTO.fromEntity(entity);
        
        // 휴가 정보
        ApprovalLeaveDTO leaveDTO = null;
        if (entity.getApprovalLeave() != null) {
            leaveDTO = ApprovalLeaveDTO.fromEntity(entity.getApprovalLeave());
        }
        
        // 지출 정보
        ApprovalExpenseDTO expenseDTO = null;
        if (entity.getApprovalExpense() != null) {
            expenseDTO = ApprovalExpenseDTO.fromEntity(entity.getApprovalExpense());
        }
        
        // 결재선 정보 (결재자 이름, 직급 포함)
        List<ApprovalLineDTO> approverDTOs = null;
        if (entity.getApprovalLines() != null && !entity.getApprovalLines().isEmpty()) {
            approverDTOs = entity.getApprovalLines().stream()
                .map(line -> ApprovalLineDTO.fromEntity(line, empRepository))
                .collect(Collectors.toList());
        }
        
        List<FileAttach> files = fileAttachRepository.findByRefTableAndRefId("APPROVAL_DOCUMENT", entity.getApprovalId());
        
        List<FileAttachDTO> attachments = null;
        if (files != null && !files.isEmpty()) {
            attachments = files.stream()
                .map(FileAttachDTO::fromEntity)
                .collect(Collectors.toList());
        }
        
        return ApprovalResponseDTO.builder()
                .document(documentDTO)
                .leave(leaveDTO)
                .expense(expenseDTO)
                .approvers(approverDTOs)
                .attachments(attachments)  // ← List<FileAttachDTO>
                .build();
    }
    
    
    // 첨부파일 정보
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private Long fileId;
        private String fileName;
        private String originFileName;
        private Long fileSize;
    }


}

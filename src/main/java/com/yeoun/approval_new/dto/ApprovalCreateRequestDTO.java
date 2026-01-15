package com.yeoun.approval_new.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalLine;
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
public class ApprovalCreateRequestDTO {
    
    private ApprovalDocumentDTO document;
    
    private ApprovalLeaveDTO leave;
    
    private ApprovalExpenseDTO expense;
    
    private List<ApprovalLineDTO> approvers;
    
    @NotBlank(message = "결재선 정보가 없습니다.")
    private String approversJson;
    
    private List<MultipartFile> attachments;
    
    private static ModelMapper modelMapper = new ModelMapper();
    /**
     * DTO → Entity (생성용)
     */
    public ApprovalDocument toEntity(EmpRepository empRepository) {
        // 1. 기본 문서 생성
        ApprovalDocument doc = document.toEntity(empRepository);
        
        // 2. 양식별 정보 추가
        if ("leave".equals(document.getFormType()) && leave != null) {
            doc.setApprovalLeave(leave.toEntity(doc));
        }
        
        if ("expense".equals(document.getFormType()) && expense != null) {
            doc.setApprovalExpense(expense.toEntity(doc));
        }
        
        // 3. 결재선 추가
        if (approvers != null && !approvers.isEmpty()) {
            List<ApprovalLine> lines = approvers.stream()
                .map(dto -> dto.toEntity(doc, empRepository))
                .collect(Collectors.toList());
            doc.setApprovalLines(lines);
        }
        
        return doc;
    }
}

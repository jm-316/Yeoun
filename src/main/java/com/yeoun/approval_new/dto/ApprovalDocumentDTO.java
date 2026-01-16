package com.yeoun.approval_new.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
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
public class ApprovalDocumentDTO {
    
    private Long approvalId;
    
    @NotBlank(message = "양식 종류를 선택해주세요.")
    private String formType;
    
    @NotBlank(message = "문서 제목을 입력해주세요.")
    @Size(max = 200, message = "문서 제목은 200자를 초과할 수 없습니다.")
    private String approvalTitle;
    
    @Size(max = 3000, message = "사유내용은 3000자를 초과할 수 없습니다.")
    private String reason;
    
    @NotNull(message = "기안일을 입력해주세요.")
    private LocalDate createdDate;
    
    @NotNull(message = "결재완료기간을 선택해주세요.")
    @FutureOrPresent(message = "결재완료기간은 오늘 이후여야 합니다.")
    private LocalDate finishDate;
    
    @NotBlank(message = "기안자 정보가 없습니다.")
    private String empId;
    
    private String empName;  // 조회용
    
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private static ModelMapper modelMapper = new ModelMapper();
    
    // Entity → DTO
    public static ApprovalDocumentDTO fromEntity(ApprovalDocument entity) {
        // 기본 매핑
        ApprovalDocumentDTO dto = modelMapper.map(entity, ApprovalDocumentDTO.class);
        
        // Emp 관계 처리
        if (entity.getEmployee() != null) {
            dto.setEmpId(entity.getEmployee().getEmpId());
            dto.setEmpName(entity.getEmployee().getEmpName());
        }
        
        return dto;
    }
    
    // DTO → Entity
    public ApprovalDocument toEntity(EmpRepository empRepository) {
        // 기본 매핑
        ApprovalDocument entity = modelMapper.map(this, ApprovalDocument.class);
        
        // Emp 관계 처리
        if (this.empId != null) {
            Emp employee = empRepository.findById(this.empId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다: " + this.empId));
            entity.setEmployee(employee);
        }
        
        // status 기본값
        if (entity.getStatus() == null) {
            entity.setStatus("PENDING");
        }
        
        return entity;
    }
}

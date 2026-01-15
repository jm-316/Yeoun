package com.yeoun.approval_new.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalLine;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.persistence.EntityNotFoundException;
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
public class ApprovalLineDTO {
    
    private Long lineId;
    private String approverId;
    private String approverName;  // 조회용
    private String posName;       // 조회용
    private Integer stepOrder;
    private String status;
    private LocalDateTime approvedAt;
    private String rejectReason;
    
    private static ModelMapper modelMapper = new ModelMapper();
    /**
     * Entity → DTO
     */
    public static ApprovalLineDTO fromEntity(ApprovalLine entity, EmpRepository empRepository) {
        ApprovalLineDTO dto = modelMapper.map(entity, ApprovalLineDTO.class);
        
        // 결재자 정보 조회
        if (entity.getApprover() != null) {
            empRepository.findById(entity.getApprover().getEmpId()).ifPresent(emp -> {
                dto.setApproverName(emp.getEmpName());
                if (emp.getPosition() != null) {
                    dto.setPosName(emp.getPosition().getPosName());
                }
            });
        }
        
        return dto;
    }
    
    /**
     * DTO → Entity
     */
    public ApprovalLine toEntity(ApprovalDocument document, EmpRepository empRepository) {
        ApprovalLine entity = modelMapper.map(this, ApprovalLine.class);
        
        Emp emp = empRepository.findById(this.approverId)
        			.orElseThrow(() -> new EntityNotFoundException("존재하지않는 사원입니다.")); 
        
        entity.setApprover(emp);
        entity.setApprovalDocument(document);
        
        if (entity.getStatus() == null) {
            entity.setStatus("PENDING");
        }
        
        return entity;
    }
}

package com.yeoun.approval_new.entity;

import java.time.LocalDateTime;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="APPROVAL_LINE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "approval_line_seq")
    @SequenceGenerator(name = "approval_line_seq", sequenceName = "SEQ_APPROVAL_LINE", allocationSize = 1)
    @Column(name = "line_id")
    private Long lineId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id", nullable = false)
    private ApprovalDocument approvalDocument;
    
//    @Column(name = "approver_id", length = 50, nullable = false) // 결재자 empId
//    private String approverId;
    // 수정: approverId → approver 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)  // DB 컬럼명 유지
    private Emp approver;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;  // 1, 2, 3
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";  // "PENDING", "APPROVED", "REJECTED"
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;
}

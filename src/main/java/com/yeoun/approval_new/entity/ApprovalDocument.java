package com.yeoun.approval_new.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yeoun.common.util.FileUtil.FileUploadHelpper;
import com.yeoun.emp.entity.Emp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="APPROVAL_DOCUMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDocument implements FileUploadHelpper{
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "approval_doc_seq")
    @SequenceGenerator(name = "approval_doc_seq", sequenceName = "SEQ_APPROVAL_DOC", allocationSize = 1)
    @Column(name = "approval_id")
    private Long approvalId;
    
    @Column(name = "form_type", length = 20, nullable = false)
    private String formType;  // "free", "leave", "expense"
    
    @Column(name = "approval_title", length = 200, nullable = false)
    private String approvalTitle;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;
    
    @Column(name = "finish_date", nullable = false)
    private LocalDate finishDate;
    
//    @Column(name = "emp_id", length = 50, nullable = false)
//    private String empId;
    // 수정: empId → Emp 관계 (FK 컬럼명은 기존 emp_id 유지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)  // DB 컬럼명 emp_id
    private Emp employee;  // Emp 타입!
    
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";  // "PENDING", "APPROVED", "REJECTED"
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 관계 설정
    @OneToOne(mappedBy = "approvalDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApprovalLeave approvalLeave;
    
    @OneToOne(mappedBy = "approvalDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApprovalExpense approvalExpense;
    
    @OneToMany(mappedBy = "approvalDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalLine> approvalLines = new ArrayList<>();
    
	// ==========================================================
	// 파일첨부 파일등록
	
	// ================================================
	@Override
	public String getTargetTable() {
		// TODO Auto-generated method stub
		return "APPROVAL_DOCUMENT";
	}

	@Override
	public Long getTargetTableId() {
		// TODO Auto-generated method stub
		return approvalId;
	}
}

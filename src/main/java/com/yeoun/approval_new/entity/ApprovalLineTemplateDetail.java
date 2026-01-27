package com.yeoun.approval_new.entity;

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
@Table(name = "APPROVAL_LINE_TEMPLATE_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLineTemplateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "approval_line_template_detail_seq")
    @SequenceGenerator(name = "approval_line_template_detail_seq", sequenceName = "SEQ_APPROVAL_LINE_TEMPLATE_DETAIL", allocationSize = 1)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ApprovalLineTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private Emp approver;  // 결재자 (Emp 참조)

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
}

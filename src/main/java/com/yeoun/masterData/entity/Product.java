package com.yeoun.masterData.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PRODUCT")
@SequenceGenerator(
		name = "PRODUCT_SEQ_GENERATOR",
		sequenceName = "PRODUCT_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Product {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_SEQ_GENERATOR")
	private Long prdId;
	
	@Column(nullable = false)
	private String prdCode; // 제품코드 ex)BG100
	
	@Column(nullable = false)
	private String prdType; // 제품타입
	
	@Column(nullable = false)
	private String prdName; // 제품명
	
	@Column(nullable = false)
	private String prdUnit; // 제품단위
	
	@Column(nullable = false)
	private Integer effectiveDate;  // 유효일자(개월)
	
	@Column(nullable = false)
	private char useYn; // 사용여부
	
	@CreatedDate
	private LocalDateTime createdDate;  // 생성일자
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp; // 생성자
	
	/**
	 * 완제품의 완제품 코드, 완제품명, 완제품 단위, 완제품 타입, 완제품 유효기간을 변경하는 메서드
	 * 
	 * @param prdCode
	 * @param prdType
	 * @param prdName
	 * @param prdUnit
	 * @param effectiveDate
	 */
	public void updateProduct(String prdCode, String prdType, String prdName, String prdUnit, Integer effectiveDate) {
		if (StringUtils.hasText(prdCode)) {
			this.prdCode = prdCode;
		}
		
		if (StringUtils.hasText(prdType)) {
			this.prdType = prdType;
		}
		
		if (StringUtils.hasText(prdName)) {
			this.prdName = prdName;
		}
		
		if (StringUtils.hasText(prdUnit)) {
			this.prdUnit = prdUnit;
		}
		
		if (effectiveDate != null) {
			this.effectiveDate = effectiveDate;
		}
	}
	
	public void chageUseYn(char useYn) {
		this.useYn = useYn;
	}
}

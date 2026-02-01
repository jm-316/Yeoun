package com.yeoun.masterData.entity;

import java.time.LocalDateTime;

import org.apache.catalina.util.StringUtil;
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
@Table(name = "MATERIAL")
@SequenceGenerator(
		name = "MATERIAL_SEQ_GENERATOR",
		sequenceName = "MATERIAL_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Material {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MATERIAL_SEQ_GENERATOR")
	private Long matId;

	@Column(name="MAT_CODE", nullable = false)
	private String matCode; // 원재료코드
	
	@Column(nullable = false)
	private String matType; // 원재료타입
	
	@Column(nullable = false)
	private String matName; // 원재료명
	
	@Column(nullable = false)
	private String matUnit; // 원재료단위
	
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
	 * 원재료의 원재료 코드, 원재료명, 원재료 단위, 완제품 타입, 원재료 유효기간을 변경하는 메서드
	 * 
	 * @param matCode
	 * @param matName
	 * @param matUnit
	 * @param effectiveDate
	 */
	public void updateMaterial(String matCode, String matName, String matUnit, Integer effectiveDate, String matType) {
		if (StringUtils.hasText(matCode)) {
			this.matCode = matCode;
		}
		
		if (StringUtils.hasText(matType)) {
			this.matType = matType;
		}
		
		if (StringUtils.hasText(matName)) {
			this.matName = matName;
		}
		
		if (StringUtils.hasText(matUnit)) {
			this.matUnit = matUnit;
		}
		
		if (effectiveDate != null) {
			this.effectiveDate = effectiveDate;
		}
	}
	
	public void chageUseYn(char useYn) {
		this.useYn = useYn;
	}
}

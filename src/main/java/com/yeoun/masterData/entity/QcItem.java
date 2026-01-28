package com.yeoun.masterData.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.masterData.dto.QcItemDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QC_ITEM")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class QcItem {

	@Id
	@Column(name="QC_ITEM_ID", length = 20)
	private String qcItemId; // QC 항목 id
	
	@Column(name="ITEM_NAME", length = 100)
	private String itemName; //항목명
	
	@Column(name="TARGET_TYPE", length = 20)
	private String targetType; //대상구분
	
	@Column(name="UNIT", length = 20)
	private String unit; //단위
	
	@Column(name="STD_TEXT", length = 200)
	private String stdText; //기준값텍스트
	
	@Column(name = "MIN_VALUE", precision = 10, scale = 2)
	private BigDecimal minValue; //허용하한값
	
	@Column(name = "MAX_VALUE", precision = 10, scale = 2)
	private BigDecimal maxValue; //허용상한값
	
	@Column(name="USE_YN", length = 1)
	private String useYn; //사용여부
	
	@Column(name = "SORT_ORDER")
	private Integer sortOrder; //정렬순서
	
	@Column(name="CREATED_ID", length = 7)
	private String createdId; //생성자 id
	
	@CreatedDate
	@Column(name="CREATED_DATE")
	private LocalDate createdDate; //생성일시
	
	@Column(name="UPDATED_ID", length = 7)
	private String updatedId; //수정자 id
	
	@Column(name="UPDATED_DATE")
	private LocalDate updatedDate; //수정일시
	
	/**
	 * QC ITEM 업데이트 메서드<br>
	 * minValue 및 maxValue의 타입 변환은 QcItem에서 변경
	 * @param qcItemDTO
	 */
	public void updateQcItem(QcItemDTO qcItemDTO) {
		this.itemName = qcItemDTO.getItemName();
		this.targetType = qcItemDTO.getTargetType();
		this.unit = qcItemDTO.getUnit();
		this.stdText = qcItemDTO.getStdText();
		this.minValue = toBigDecimal(qcItemDTO.getMinValue());
		this.maxValue = toBigDecimal(qcItemDTO.getMaxValue());
		this.sortOrder = Integer.parseInt(qcItemDTO.getSortOrder());
		this.useYn = qcItemDTO.getUseYn();
		this.updatedId = qcItemDTO.getUpdateId();
	}
	
	/**
	 * String 타입으로 받은 minValue와 maxValue를 BigDecimal로 변경
	 * 
	 * @param minValue
	 * @param maxValue
	 */
	public void toBigDeciaml(String minValue, String maxValue) {
		this.minValue = toBigDecimal(minValue);
		this.maxValue = toBigDecimal(maxValue);
	}
	
	// String으로 들어오는 값을 BigDecimal으로 변경
	private BigDecimal toBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		} 
		
		return new BigDecimal(value);
	}
}

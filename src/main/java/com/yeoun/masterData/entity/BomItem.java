package com.yeoun.masterData.entity;

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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "BOM_ITEM")
@SequenceGenerator(
		name = "BOM_ITEM_SEQ_GENERATOR",
		sequenceName = "BOM_ITEM_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
public class BomItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOM_ITEM_SEQ_GENERATOR")
	private Long bomItemId;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "BOM_ID", nullable = false)
	private Bom bom; // BOM ID
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "MAT_ID", nullable = false)
	private Material material; // 원재료 ID
	
	@Column(nullable = false)
	private String bomUnit; // 사용 단위
	
	@Column(nullable = false)
	private Double bomQty;  // 소모량
}

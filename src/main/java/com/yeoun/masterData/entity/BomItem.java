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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Builder
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
	
//	@Column(nullable = false)
//	private char useYn; // 사용여부

	public BomItem(Long bomItemId, Bom bom, Material material, String bomUnit, 
			Double bomQty) {
		this.bomItemId = bomItemId;
		this.bom = bom;
		this.material = material;
		this.bomUnit = bomUnit;
		this.bomQty = bomQty;
//		this.useYn = useYn;
	}
	
	public void updatBomItem(Double bomQty) {
		this.bomQty = bomQty;
	}
}

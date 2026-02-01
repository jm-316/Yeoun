package com.yeoun.masterData.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "BOM")
@SequenceGenerator(
		name = "BOM_MASTER_SEQ_GENERATOR",
		sequenceName = "BOM_MASTER_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Bom {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOM_MASTER_SEQ_GENERATOR")
	private Long bomId; 
	
	@Column(nullable = false)
	private String bomName; // bom이름
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PRD_ID", nullable = false)
	private Product product; // 제품ID
	
	@Column(nullable = false)
	private char useYn; // 사용여부
	
	@CreatedDate
	private LocalDateTime createdDate;  // 생성일자
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp; // 생성자
	
	@OneToMany(mappedBy = "bom", cascade = CascadeType.ALL)
	private List<BomItem> bomItems = new ArrayList<>();
	
	public void addBommItem(BomItem bomItem) {
		this.bomItems.add(bomItem);
		bomItem.setBom(this);
	}
}

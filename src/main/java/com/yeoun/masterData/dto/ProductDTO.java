package com.yeoun.masterData.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.entity.Product;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProductDTO {
	private Long prdId;
	
	@NotBlank(message = "완제품 코드는 필수 입력값입니다.")
	private String prdCode; // 완제품 코드
	
	@NotBlank(message = "완제품 유형은 필수 입력값입니다.")
	private String prdType; // 완제품 타입
	
	@NotBlank(message = "완제품 품목명은 필수 입력값입니다.")
	private String prdName; // 완제품명
	
	@NotBlank(message = "단위는 필수 입력값입니다.")
	private String prdUnit; // 완제품단위
	
	@NotBlank(message = "유효일자는 필수 입력값입니다.")
	private Integer effectiveDate;  // 유효일자(개월)
	
	private char useYn; // 사용여부
	
	private String empId; // 생성자
	
	private LocalDateTime createdDate;
	
	@Builder
	public ProductDTO(Long prdId, String prdCode, String prdType,
			String prdName, String prdUnit,Integer effectiveDate, char useYn, String empId) {
		this.prdId = prdId;
		this.prdCode = prdCode;
		this.prdType = prdType;
		this.prdName = prdName;
		this.prdUnit = prdUnit;
		this.effectiveDate = effectiveDate;
		this.useYn = useYn;
		this.empId = empId;
	}
	
	// -----------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// Entity 타입으로 변환
	public Product toEntity() {
		return modelMapper.map(this, Product.class);
	}
	
	// DTO 타입으로 변환
	public static ProductDTO fromEntity(Product product) {
		ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
		
		productDTO.setEmpId(product.getEmp().getEmpId());
		
		return productDTO;
	}

}

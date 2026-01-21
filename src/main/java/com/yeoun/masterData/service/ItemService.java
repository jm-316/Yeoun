package com.yeoun.masterData.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.dto.ProductDTO;
import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.repository.BomRepository;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ItemService {

	private final MaterialRepository materialRepository;
	private final ProductRepository productRepository;
	private final BomRepository bomRepository;
	
	// 원재료 목록 조회
	public List<MaterialDTO> getMaterialList() {
		return materialRepository.findAll()
				.stream()
				.map(MaterialDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 원재료 조회 (활성화된 내역만)
	public List<MaterialDTO> getMaterialListWithUseYn(String useYnStr) {
		Character useYn = useYnStr.charAt(0);
		
		return materialRepository.findByUseYn(useYn)
				.stream()
				.map(MaterialDTO::fromEntity)
				.collect(Collectors.toList());
	}

	// 원재료 등록
	@Transactional
	public void createMaterial(List<MaterialDTO> materialDTOList, String empId) {
		List<Material> materials = materialDTOList.stream()
	            .map(dto -> MaterialDTO.builder()
                .matCode(dto.getMatCode())
                .matType(dto.getMatType())
                .matName(dto.getMatName())
                .matUnit(dto.getMatUnit())
                .effectiveDate(dto.getEffectiveDate())
                .useYn(dto.getUseYn())
                .empId(empId)
                .build()
                .toEntity())
	            .collect(Collectors.toList());
		
		materialRepository.saveAll(materials);
	}

	// 원재료 수정
	@Transactional
	public void updateMaterial(List<MaterialDTO> updatedRows) {
		for (MaterialDTO dto : updatedRows) {
			Material material = materialRepository.findByMatCode(dto.getMatCode())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원재료입니다."));
			
			if ('N' == dto.getUseYn() && 'Y' == material.getUseYn()) {
//				validateDeactivation(material);
			}
		}
	}
	
	/**
	 * 비활성화 가능 여부 검증 메서드 <br>
	 * 활성되된 BOM을 사용하는 생산 계획 / 작업 있는지 확인<br>
	 * 해당 원재료를 사용하는 제품의 수주가 있는지 확인
	 * 
	 * @param material
	 */
//	private void validateDeactivation(Material material) {
//		boolean isUsedInBom = bomRepository.existsByMatCodeAndUseYn(material.getMatCode(), 'Y');
//	}
	
	// ============================================
	// 완제품 조회
	public List<ProductDTO> getProductList() {
		return productRepository.findAll()
				.stream()
				.map(ProductDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 완제품 조회(활성여부)
	public List<ProductDTO> getProductListWithUseYn(String useYnStr) {
		Character useYn = useYnStr.charAt(0);
		return productRepository.findByUseYn(useYn)
				.stream()
				.map(ProductDTO::fromEntity)
				.collect(Collectors.toList());
	}

	// 완제품 신규 등록
	@Transactional
	public void createProduct(List<ProductDTO> productDTOList, String empId) {
		List<Product> products = productDTOList.stream()
	            .map(dto -> ProductDTO.builder()
                .prdCode(dto.getPrdCode())
                .prdType(dto.getPrdType())
                .prdName(dto.getPrdName())
                .prdUnit(dto.getPrdUnit())
                .effectiveDate(dto.getEffectiveDate())
                .useYn(dto.getUseYn())
                .empId(empId)
                .build()
                .toEntity())
	            .collect(Collectors.toList());
		
		productRepository.saveAll(products);
		
	}

	// 완제품 수정
	public void updateProduct(List<ProductDTO> updatedRows) {
		// TODO Auto-generated method stub
		
	}
}

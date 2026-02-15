package com.yeoun.masterData.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.dto.ProductDTO;
import com.yeoun.masterData.dto.SafeStockDTO;
import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.mapper.SafeStockMapper;
import com.yeoun.masterData.repository.BomItemRepository;
import com.yeoun.masterData.repository.BomRepository;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.ProductRepository;
import com.yeoun.masterData.repository.SafeStockRepository;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.production.enums.ProductionStatus;
import com.yeoun.production.repository.ProductionPlanRepository;
import com.yeoun.sales.enums.OrderItemStatus;
import com.yeoun.sales.repository.OrderItemRepository;

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
	private final BomItemRepository bomItemRepository;
	private final InventoryRepository inventoryRepository;
	private final ProductionPlanRepository productionPlanRepository;
	private final OrderItemRepository orderItemRepository;
	private final WorkOrderRepository workOrderRepository;
	private final SafeStockRepository safeStockRepository;
	private final SafeStockMapper safeStockMapper;
	
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
			Material material = materialRepository.findByMatId(dto.getMatId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원재료입니다."));
			
			if ('N' == dto.getUseYn() && 'Y' == material.getUseYn()) {
				List<Long> prdIds  = bomItemRepository.findProductIdsByMaterialId(dto.getMatId());
				
				List<String> prdCodes = productRepository.findPrdCodesByPrdId(prdIds);
				
				if (!prdCodes.isEmpty()) {
					// 생산계획에서 사용하는지 확인
					boolean usedInPlan = productionPlanRepository.existsByPrdIdsInAndStatusIn(
							prdCodes,
							List.of(ProductionStatus.PLANNING,
									ProductionStatus.MATERIAL_PENDING,
									ProductionStatus.IN_PROGRESS
							)
						);
					
					if (usedInPlan) {
						throw new IllegalStateException("해당 원재료를 사용하는 제품의 생산계획이 존재합니다.");
					}
					
					boolean usedInOrder = orderItemRepository.existsByPrdIdsInAndStatusIn(
								prdCodes,
								List.of(OrderItemStatus.REQUEST,
										OrderItemStatus.CONFIRMED,
										OrderItemStatus.PLANNED
								)
							);
					if (usedInOrder) {
						 throw new IllegalStateException("해당 원재료를 사용하는 제품의 수주가 존재합니다.");
					}
					
					List<String> statuses = List.of("CREATED", "RELEASED", "IN_PROGRESS");
					
					boolean usedInWorkOrdr = workOrderRepository.existsByPrdIdsInAndStatusIn(
								prdCodes,
								statuses
							);
					
					if (usedInWorkOrdr) {
						  throw new IllegalStateException("해당 자재를 사용하는 제품이 현재 생산 중(작업지시)입니다.");
					}
				}
				
				safeStockRepository.deleteByItemId(dto.getMatId());
			}
			material.updateMaterial(dto.getMatCode(), dto.getMatName(), dto.getMatUnit(), dto.getEffectiveDate(), dto.getMatUnit());
			material.chageUseYn(dto.getUseYn());
		}
	}
	
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
	@Transactional
	public void updateProduct(List<ProductDTO> updatedRows) {
		for (ProductDTO dto : updatedRows) {
			Product product = productRepository.findByPrdId(dto.getPrdId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 완제품 입니다."));
			
			if ('N' == dto.getUseYn() && 'Y' == product.getUseYn()) {
				
				String prdCode = product.getPrdCode();
				
				boolean usedInPlan = productionPlanRepository.existsByPrdIdAndStatusIn(
						prdCode,
						List.of(ProductionStatus.PLANNING,
								ProductionStatus.MATERIAL_PENDING,
								ProductionStatus.IN_PROGRESS
						)
					);
				
				if (usedInPlan) {
					throw new IllegalStateException("해당 완제품을 사용하는 생산계획이 존재합니다.");
				}
				
				boolean usedInOrder = orderItemRepository.existsByPrdIdAndStatusIn(
							prdCode,
							List.of("REQUEST", "CONFIRMED", "PLANNED")
						);
						
				if (usedInOrder) {
					 throw new IllegalStateException("해당 제품의 수주가 존재합니다.");
				}
				
				List<String> statuses = List.of("CREATED", "RELEASED", "IN_PROGRESS");
				
				boolean usedInWorkOrdr = workOrderRepository.existsByPrdIdAndStatusIn(
							prdCode,
							statuses
						);
					
				if (usedInWorkOrdr) {
					  throw new IllegalStateException("해당 제품이 현재 생산 중(작업지시)입니다.");
				}
				
				safeStockRepository.deleteByItemId(dto.getPrdId());
			}
			product.updateProduct(dto.getPrdCode(),dto.getPrdType(), dto.getPrdName(), dto.getPrdUnit(), dto.getEffectiveDate());
			product.chageUseYn(dto.getUseYn());
		}
		
	}

	// 원재료 코드 수정 가능 여부 확인
	public boolean isMatCodeInUse(String matCode) {
		Optional<Material> optional = materialRepository.findByMatCode(matCode);
		
		// 신규 등록인 경우 사용 중일 수가 없어서 false 처리
		if (optional.isEmpty()) {
			return false;
		}
		
		Material material = optional.get();
		
		// BOM Item에 등록되어 있는지
		int bomCount = bomItemRepository.countByMaterialMatId(material.getMatId());
		
		// 재고가 있는지
		int stockCount = inventoryRepository.countByItemId(matCode);
		
		return (bomCount > 0 || stockCount > 0);
	}

	// 완제품 코드 수정 가능 여부 확인
	public boolean isPrdCodeInUse(String prdCode) {
		Product product = productRepository.findByPrdCode(prdCode)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 완제품입니다."));
		
		// BOM에 등록되어 있는지 확인
		int bomCount = bomRepository.countByProductPrdId(product.getPrdId());
		
		// 재고가 있는지 확인
		int stockCount = inventoryRepository.countByItemId(prdCode);
		
		return (bomCount > 0 || stockCount > 0);
	}

	// 원재료 및 완제품 조회(활성화된 품목들만)
	public List<SafeStockDTO> findAllItems() {
		return safeStockMapper.findAllItem();
	}
}

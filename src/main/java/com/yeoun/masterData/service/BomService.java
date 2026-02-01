package com.yeoun.masterData.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.masterData.dto.BomDTO;
import com.yeoun.masterData.dto.BomItemDTO;
import com.yeoun.masterData.entity.Bom;
import com.yeoun.masterData.entity.BomItem;
import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.mapper.BomMstMapper;
import com.yeoun.masterData.repository.BomItemRepository;
import com.yeoun.masterData.repository.BomRepository;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.ProductRepository;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.outbound.dto.OutboundOrderItemDTO;
import com.yeoun.production.enums.ProductionStatus;
import com.yeoun.production.repository.ProductionPlanRepository;
import com.yeoun.sales.enums.OrderItemStatus;
import com.yeoun.sales.repository.OrderItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BomService {
	private final BomRepository bomRepository;
	private final BomItemRepository bomItemRepository;
	private final ProductRepository productRepository;
	private final MaterialRepository materialRepository;
	private final EmpRepository empRepository;
	private final ProductionPlanRepository productionPlanRepository;
	private final OrderItemRepository orderItemRepository;
	private final WorkOrderRepository workOrderRepository;
	private final BomMstMapper bomMstMapper;
	
	// Bom 조회
	public List<BomDTO> getBomList() {
		
		return bomRepository.findAll()
				.stream()
				.map(BomDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// BOM 등록
	@Transactional
	public void createBom(BomDTO data, String empId) {
		Product product = productRepository.findByPrdId(data.getPrdId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제품입니다."));
		
		Emp emp = empRepository.findByEmpId(empId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제품입니다."));
		
		BomDTO bomDTO = BomDTO.builder()
				.bomName(data.getBomName())
				.useYn('Y')
				.build();
		
		List<BomItemDTO> items = new ArrayList<>();
		
		// 필요한 원재료 ID 목록 추출
		List<Long> matIds = data.getItems().stream()
				.map(BomItemDTO::getMatId)
				.collect(Collectors.toList());
		
		// 필요한 원재료 한번에 조회
		List<Material> materials = materialRepository.findByMatIdIn(matIds);
		
		// matId 기준으로 원재료 그룹핑
		Map<Long, Material> materialMap = materials.stream()
				.collect(Collectors.toMap(Material::getMatId, m -> m));
		
		// 원재료 존재 유무 체크
		for (Long matId : matIds) {
			if (!materialMap.containsKey(matId)) {
				 throw new IllegalArgumentException("존재하지 않는 원재료입니다. matId: " + matId);
			}
		}
			
		for (BomItemDTO bomitemDTO : data.getItems()) {
			BomItemDTO dto = BomItemDTO.builder()
					.matId(bomitemDTO.getMatId())
					.bomUnit(bomitemDTO.getBomUnit())
					.bomQty(bomitemDTO.getBomQty())
					.build();
			
			items.add(dto);
		}
		
		Bom bom = bomDTO.toEntity();
		bom.setProduct(product);
		bom.setEmp(emp);
		
		for (BomItemDTO item : items) {
			BomItem bomItem = item.toEntity();
			
			Material material = materialMap.get(item.getMatId());
			
			bomItem.setMaterial(material);
			
			bom.addBommItem(bomItem);
		}
		
		bomRepository.save(bom);
		
	}
	
	// BOM 수정
	@Transactional
	public void modifyBom(List<BomDTO> data) {
		for (BomDTO dto : data) {
			Bom bom = bomRepository.findByBomId(dto.getBomId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 BOM 입니다."));
			
			if ('N' == dto.getUseYn() && 'Y' == bom.getUseYn()) {
				String prdCode = bom.getProduct().getPrdCode();
				
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
			}
			bom.setBomName(dto.getBomName());
			bom.setUseYn(dto.getUseYn());
		}
		
	}

	// Bom Item 조회
	public List<BomItemDTO> getBomItemList(Long bomId) {
		return bomItemRepository.findAllByBom_bomId(bomId)
				.stream()
				.map(BomItemDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// Bom Item 등록
	@Transactional
	public void createBomItem(List<BomItemDTO> BomItemDTOList, String empId) {
		if (BomItemDTOList == null || BomItemDTOList.isEmpty()) {
			throw new IllegalArgumentException("BomItem 목록이 비어있습니다.");
		}
		
		List<BomItem> bomItems = new ArrayList<>();
		
		for (BomItemDTO dto : BomItemDTOList) {
			if (dto.getBomId() == null || dto.getMatId() == null) {
				throw new IllegalArgumentException("BomId 또는 MatId가 null입니다.");
			}
			
			Bom bom = bomRepository.findByBomId(dto.getBomId())
					.orElseThrow(() -> new RuntimeException("BOM을 찾을 수 없습니다: " + dto.getBomId()));
			
			Material material = materialRepository.findByMatId(dto.getMatId())
					.orElseThrow(() -> new RuntimeException("원재료를 찾을 수 없습니다: " + dto.getBomId()));
			
			BomItem bomItem = BomItem.builder()
					.bom(bom)
					.material(material)
					.bomQty(dto.getBomQty())
					.bomUnit(dto.getBomUnit())
					.build();
			
			bomItems.add(bomItem);
		}
		bomItemRepository.saveAll(bomItems);
	}

	// Bom Item 수정
	@Transactional
	public void updateMaterial(List<BomItemDTO> updatedRows) {
		for (BomItemDTO dto : updatedRows) {
			BomItem bomItem = bomItemRepository.findByBomItemId(dto.getBomItemId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 BOM Item 입니다."));
			
			Bom bom = bomRepository.findByBomId(bomItem.getBom().getBomId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 BOM 입니다."));
			
			boolean isCriticalChange =
					!Objects.equals(bomItem.getMaterial().getMatId(), dto.getMatId())
					|| bomItem.getBomQty().compareTo(dto.getBomQty()) != 0
					|| !Objects.equals(bomItem.getBomUnit(), dto.getBomUnit());
			
			if (isCriticalChange) {
				validateBomItemUsage(bom.getProduct().getPrdCode());
			}
			
			bomItem.updatBomItem(dto.getBomQty());
		}
		
	}
	
	/**
	 * 수주, 생산계획, 작업지시에서 해당 BOM을 사용하는 제품이 있는지 확인하는 메서드
	 * 
	 * @param prdCode
	 */
	public void validateBomItemUsage(String prdCode) {
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
	}

	// bom Item 삭제
	@Transactional
	public void deleteBomItems(List<String> bomItemIds) {
		List<Long> ids = bomItemIds.stream()
				 .map((String id) -> Long.valueOf(id))
				 .collect(Collectors.toList());
		
		List<BomItem> bomItems = bomItemRepository.findAllByBomItemIdIn(ids);
		
		if (bomItems.size() != ids.size()) {
			 throw new IllegalArgumentException("존재하지 않는 BOM Item이 포함되어 있습니다.");
		}
		
		Set<String> prdCodes = bomItems.stream()
				.map(bomItem -> bomItem.getBom().getProduct().getPrdCode())
				.collect(Collectors.toSet());
		
		for (String prdCode : prdCodes) {
			validateBomItemUsage(prdCode);
		}
		
		bomItemRepository.deleteAllByBomItemIdIn(ids);
	}

	// BOM Name 중복 검사
	public boolean existsByBomName(String bomName) {
		return bomRepository.existsByBomName(bomName);
	}
	
	public List<OutboundOrderItemDTO> getBomListByPrdId(String prdId) {
		return bomMstMapper.findByPrdIdList(prdId);
	}
}

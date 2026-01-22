package com.yeoun.masterData.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.yeoun.masterData.repository.BomItemRepository;
import com.yeoun.masterData.repository.BomRepository;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.ProductRepository;

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
	public void updateMaterial(List<BomItemDTO> updatedRows) {
		// TODO Auto-generated method stub
		
	}

	// bom Item 삭제
	@Transactional
	public void deleteBomItems(List<String> bomItemIds) {
		List<Long> ids = bomItemIds.stream()
				.map(Long::valueOf)
				.collect(Collectors.toList());
		
		bomItemRepository.deleteAllByBomItemIdIn(ids);
	}

	// BOM Name 중복 검사
	public boolean existsByBomName(String bomName) {
		return bomRepository.existsByBomName(bomName);
	}

}

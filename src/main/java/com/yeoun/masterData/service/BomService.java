package com.yeoun.masterData.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
	
	// Bom 조회
	public List<BomDTO> getBomList() {
		
		return bomRepository.findAll()
				.stream()
				.map(BomDTO::fromEntity)
				.collect(Collectors.toList());
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

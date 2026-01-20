package com.yeoun.masterData.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.masterData.dto.BomDTO;
import com.yeoun.masterData.dto.BomItemDTO;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.repository.BomItemRepository;
import com.yeoun.masterData.repository.BomRepository;
import com.yeoun.masterData.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BomService {
	private final BomRepository bomRepository;
	private final BomItemRepository bomItemRepository;
	private final ProductRepository productRepository;
	
	// bom 조회
	public List<BomDTO> getBomList() {
		
		return bomRepository.findAll()
				.stream()
				.map(BomDTO::fromEntity)
				.collect(Collectors.toList());
	}

	public List<BomItemDTO> getBomItemList(Long bomId) {
		return bomItemRepository.findAllByBom_bomId(bomId)
				.stream()
				.map(BomItemDTO::fromEntity)
				.collect(Collectors.toList());
	}

}

package com.yeoun.masterData.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ItemService {

	private final MaterialRepository materialRepository;
	private final ProductRepository productRepository;
	
	// 원재료 목록 조회
	public List<MaterialDTO> getMaterialList() {
		return materialRepository.findAll()
				.stream()
				.map(MaterialDTO::fromEntity)
				.collect(Collectors.toList());
	}
}

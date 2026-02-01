package com.yeoun.masterData.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.masterData.dto.SafeStockDTO;
import com.yeoun.masterData.entity.SafeStock;
import com.yeoun.masterData.mapper.SafeStockMapper;
import com.yeoun.masterData.repository.SafeStockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class SafeStockService {
	
	private final SafeStockRepository safeStockRepository;
	private final SafeStockMapper safeStockMapper;
	
	// 안전재고 조회
	public List<SafeStockDTO> getSafeStockList() {
		return safeStockMapper.findAllSafeStock();
	}

	// 안전재고 등록
	@Transactional
	public void createSafeStock(List<SafeStockDTO> createdRows) {
		List<SafeStock> safeStocks = createdRows.stream()
				.map(dto -> SafeStockDTO.builder()
				.itemId(dto.getItemId())
				.itemType(dto.getItemType())
				.itemUnit(dto.getItemUnit())
				.dailyCapa(dto.getDailyCapa())
				.dailyReqQty(dto.getDailyReqQty())
				.targetDays(dto.getTargetDays())
				.totalSafeQty(dto.getTotalSafeQty())
				.useYn('Y')
				.remark(dto.getRemark())
				.build()
				.toEntity())
				.collect(Collectors.toList());
				
		safeStockRepository.saveAll(safeStocks);
	}

	// 안전재고 수정
	@Transactional
	public void updateSafeStock(List<SafeStockDTO> updatedRows) {
		for (SafeStockDTO dto : updatedRows) {
			SafeStock safeStock = safeStockRepository.findBySafeId(dto.getSafeId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안전재고입니다."));
			
			safeStock.setDailyCapa(dto.getDailyCapa());
			safeStock.updateSafeCriteria(dto.getDailyReqQty(), dto.getTargetDays());
		}
	}

	// 안전재고 삭제
	@Transactional
	public void deleteSafeStock(List<String> data) {
		List<Long> ids = data.stream()
				.map((String id) -> Long.valueOf(id))
				.collect(Collectors.toList());
		
		List<SafeStock> safeStocks = safeStockRepository.findAllBySafeIdIn(ids);
		
		if (safeStocks.size() != ids.size()) {
			throw new IllegalArgumentException("존재하지 않는 안전재고가 포함되어 있습니다.");
		}
		
		safeStockRepository.deleteAllBySafeIdIn(ids);
	}
	
}

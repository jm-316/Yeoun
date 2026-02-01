package com.yeoun.masterData.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yeoun.masterData.dto.SafeStockDTO;
import com.yeoun.masterData.entity.SafeStock;

@Mapper
public interface SafeStockMapper {

	// 안전재고 조회
	List<SafeStockDTO> findAllSafeStock();

	// 원재료 및 완제품 활성화된 내역만 조회
	List<SafeStockDTO> findAllItem();
}

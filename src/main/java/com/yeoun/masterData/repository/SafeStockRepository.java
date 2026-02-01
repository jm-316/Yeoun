package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.entity.SafeStock;

public interface SafeStockRepository extends JpaRepository<SafeStock, Long> {

	// safeId로 조회
	Optional<SafeStock> findBySafeId(Long safeId);

	// safeId로 목록 조회
	List<SafeStock> findAllBySafeIdIn(List<Long> ids);
	
	// safeId로 삭제
	void deleteAllBySafeIdIn(List<Long> ids);

	// itemId로 삭제
	void deleteByItemId(Long matId);
}

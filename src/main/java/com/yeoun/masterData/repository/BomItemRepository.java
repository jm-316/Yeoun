package com.yeoun.masterData.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.BomItem;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long>{

	// Bom Item 조회
	List<BomItem> findAllByBom_bomId(Long bomId);
	
	@Query("DELETE FROM BomItem b WHERE b.bomItemId IN :bomItemIds")
	void deleteAllByBomItemIdIn(@Param("bomItemIds") List<Long> bomItemIds);
  
	
}

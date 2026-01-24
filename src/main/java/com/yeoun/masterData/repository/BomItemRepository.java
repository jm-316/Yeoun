package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.BomItem;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long>{

	// Bom Item 조회
	List<BomItem> findAllByBom_bomId(Long bomId);
	 
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM BomItem b WHERE b.bomItemId IN :bomItemIds")
	void deleteAllByBomItemIdIn(@Param("bomItemIds") List<Long> bomItemIds);

	// BOM ITEM에 원재료 등록되었는지 카운팅
	int countByMaterialMatId(Long matId);

	// matId로 productId 조회
	@Query("SELECT b.product.prdId FROM BomItem bi JOIN bi.bom b WHERE bi.material.id = :matId")
	List<Long> findProductIdsByMaterialId(@Param("matId") Long matId);

	// bomItemId로 조회
	Optional<BomItem> findByBomItemId(Long bomItemId);

	// bomItemId로 bomItem 조회
	List<BomItem> findAllByBomItemIdIn(List<Long> ids);
}

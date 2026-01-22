package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Bom;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long>{

	Optional<Bom> findByBomId(Long bomId);

	// BOM Name 중복 검사
	boolean existsByBomName(String bomName);

	// 제품 ID로 BOM COUNT
	int countByProductPrdId(Long prdId);

	// 제품 ID로 BOM에서 PRDID 조회
	@Query("SELECT b.product.prdId FROM Bom b WHERE b.product.id = :prdId")
	List<Long> findProductIdByProductId(@Param("prdId") Long prdId);
  
	
}

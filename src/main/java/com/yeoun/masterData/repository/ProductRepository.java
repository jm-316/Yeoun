package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 활성 여부로 완제품 조회
	List<Product> findByUseYn(Character useYn);

	// 제품 ID로 조회
	Optional<Product> findByPrdId(Long prdId);
}

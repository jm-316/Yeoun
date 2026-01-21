package com.yeoun.masterData.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 활성 여부로 완제품 조회
	List<Product> findByUseYn(Character useYn);
}

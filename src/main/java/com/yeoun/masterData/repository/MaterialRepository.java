package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

	// 원재료 코드로 원재료 조회
	Optional<Material> findByMatCode(String matCode);

	List<Material> findByUseYn(Character useYn1);

	// 원재료Id로 원재료 조회
	Optional<Material> findByMatId(Long matId);

	// 원재료 ID로 원재료 목록 조회
	List<Material> findByMatIdIn(List<Long> matIds);

	
}

package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

	// 원재료 코드로 원재료 조회
	Optional<Material> findByMatCode(String matCode);

	List<Material> findByUseYn(Character useYn1);

	
}

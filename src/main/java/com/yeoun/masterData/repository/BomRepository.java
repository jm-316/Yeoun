package com.yeoun.masterData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Bom;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long>{

	Optional<Bom> findByBomId(Long bomId);
  
	
}

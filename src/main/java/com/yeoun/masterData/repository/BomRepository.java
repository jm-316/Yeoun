package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Bom;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long>{
  
	
}

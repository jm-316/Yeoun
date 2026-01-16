package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

	
}

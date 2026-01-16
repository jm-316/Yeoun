package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {



}

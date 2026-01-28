package com.yeoun.sales.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.sales.dto.ClientItemDTO;
import com.yeoun.sales.entity.ClientItem;
import com.yeoun.sales.repository.ClientItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientItemService {
  
    private final MaterialRepository materialRepository;
    private final ClientItemRepository clientItemRepository;
    

    @Transactional
    public void addItems(String clientId, List<ClientItemDTO> items, String empId) {

        for (ClientItemDTO dto : items) {
        	ClientItem item = ClientItem.builder()
        	        .clientId(clientId)
        	        .materialId(dto.getMaterialId())
        	        .unitPrice(dto.getUnitPrice())
        	        .minOrderQty(dto.getMoq())
        	        .unit(dto.getUnit())
        	        .orderUnit(dto.getOrderUnit())   
        	        .leadDays(dto.getLeadDays())     
        	        .supplyAvailable(dto.getSupplyAvailable())
        	        .createdAt(LocalDateTime.now())
        	        .createdBy(empId)
        	        .build();


        	clientItemRepository.save(item);
        }
    }

    /** 🔥 품명 + 단위까지 포함된 DTO 목록 반환 */
    public List<ClientItemDTO> getItems(String clientId) {
        return clientItemRepository.findItemsWithMaterialInfo(clientId);
    }
   
    
    /**
     * 🔥 협력사에 아직 등록되지 않은 자재 목록
     */
    public List<Material> getAvailableMaterials(
            String clientId,
            String matType
    ) {

        // 1️⃣ 해당 협력사에 이미 등록된 materialId 목록
        List<String> registeredMaterialIds =
                clientItemRepository.findMaterialIdsByClientId(clientId);
        

        // 2️⃣ 카테고리별 전체 자재
        List<Material> allMaterials =
        		materialRepository.findByMatTypeAndUseYn(matType, "Y");

        // 3️⃣ 이미 등록된 자재 제외
        return allMaterials.stream()
                .filter(m -> !registeredMaterialIds.contains(m.getMatId()))
                .toList();
    }
    
    
}

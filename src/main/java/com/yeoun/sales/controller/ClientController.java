package com.yeoun.sales.controller;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.Material;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.sales.dto.ClientItemDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.sales.entity.ClientItem;
import com.yeoun.sales.repository.ClientItemRepository;
import com.yeoun.sales.service.ClientItemService;
import com.yeoun.sales.service.ClientService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/client")
public class ClientController {

    private final ClientService clientService;
    private final ClientItemService itemService;
    private final MaterialRepository materialRepository;
    private final ClientItemRepository itemRepository;  


    /* ======================================================
       1. 거래처/협력사 목록 페이지 (HTML)
    ====================================================== */
    @GetMapping
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "itemKeyword", required = false) String itemKeyword,
            @RequestParam(value = "type", required = false, defaultValue = "CUSTOMER") String type,
            Model model
    ) {
        List<Client> list = clientService.search(keyword, itemKeyword, type);

        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("itemKeyword", itemKeyword);
        model.addAttribute("type", type);

        return "sales/client_list";
    }


    /* ======================================================
       2. 목록 JSON API (AG Grid 전용)
    ====================================================== */
    @GetMapping("/data")
    @ResponseBody
    public List<Client> listData(
            @RequestParam(value="keyword", required = false) String keyword,
            @RequestParam(value="itemKeyword", required = false) String itemKeyword,
            @RequestParam(value="type", required = false, defaultValue = "CUSTOMER") String type
    ) {
        return clientService.search(keyword, itemKeyword, type);
    }



    /* ======================================================
       3. 상세 페이지 (CUSTOMER / SUPPLIER HTML 분리)
    ====================================================== */
    @GetMapping("/{clientId}")
    public String detailPage(
            @PathVariable("clientId") String clientId,
            Model model,
            RedirectAttributes rttr
    ) {
        Client client = clientService.get(clientId);

        if (client == null) {
            rttr.addFlashAttribute("msg", "존재하지 않는 거래처입니다.");
            return "redirect:/sales/client";
        }

        model.addAttribute("client", client);

        // ▶ CUSTOMER → 고객 상세 페이지
        if ("CUSTOMER".equalsIgnoreCase(client.getClientType())) {
            return "sales/client_detail";
        }

        // ▶ SUPPLIER → 협력사 상세 페이지
        model.addAttribute("items", itemService.getItems(clientId));
        model.addAttribute("materials", materialRepository.findAll());
        
        return "sales/supplier_detail";
    }


    /* ======================================================
       4. 상세정보 JSON API
    ====================================================== */
    @GetMapping("/detail/{clientId}")
    @ResponseBody
    public Client detailData(@PathVariable("clientId") String clientId) {
        return clientService.get(clientId);
    }


    /* ======================================================
       5. 거래처 등록 페이지
    ====================================================== */
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("client", new Client());
        return "sales/client_create";
    }


    /* ======================================================
       6. 거래처 등록 프로세스
    ====================================================== */
    @PostMapping("/create")
    public String createProcess(
            @ModelAttribute Client client,
            RedirectAttributes rttr
    ) {
        try {
            clientService.create(client);
            rttr.addFlashAttribute("msg", "거래처가 등록되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "오류: " + e.getMessage());
            return "redirect:/sales/client/create";
        }

        return "redirect:/sales/client";
    }


    /* ======================================================
       7. 사업자번호 중복 확인
    ====================================================== */
    @GetMapping("/check-business")
    @ResponseBody
    public boolean checkBusiness(@RequestParam("businessNo") String businessNo) {

        String cleanBiz = businessNo.replaceAll("[^0-9]", ""); // 숫자만 남기는 정규식

        return !clientService.existsByBusinessNoClean(cleanBiz);
        // true = 사용 가능 / false = 중복
    }
    
    
    /* ======================================================
        8. 협력사 취급제품
    ====================================================== */
    
    @PostMapping("/{clientId}/items")
    @ResponseBody
    public String saveItems(
            @PathVariable("clientId") String clientId,
            @RequestBody List<ClientItemDTO> items,
            @AuthenticationPrincipal LoginDTO login
    ) {
        itemService.addItems(clientId, items, login.getEmpId());
        return "OK";
    }
    
    @GetMapping("/material/data")
    @ResponseBody
    public List<Material> getMaterialList() {
        return materialRepository.findAll();
    }

    
    
    /* ======================================================
        9. 협력사 취급제품 등록
    ====================================================== */
  
    
    @GetMapping("/{clientId}/items/create")
    public String itemCreatePage(
            @PathVariable("clientId") String clientId,
            @RequestParam(value="cat", defaultValue="RAW") String category,
            Model model
    ){
        // 🔥 이미 등록된 품목 제외된 자재만 조회
        List<Material> list =
                itemService.getAvailableMaterials(clientId, category);

        model.addAttribute("clientId", clientId);
        model.addAttribute("materials", list);
        model.addAttribute("category", category);

        return "sales/supplier_item_create";
    }


    

    @PostMapping("/{clientId}/items/create")
    public String itemCreateProcess(
            @PathVariable("clientId") String clientId,
            @RequestParam("materialId") String materialId,
            @RequestParam("unitPrice") BigDecimal unitPrice,
            @RequestParam("moq") BigDecimal moq,
            @RequestParam("supplyAvailable") String supplyAvailable,
            @RequestParam("unit") String unit,
            @RequestParam("orderUnit") BigDecimal orderUnit,
            @RequestParam("leadDays") BigDecimal leadDays,
            @RequestParam("category") String category,
            @AuthenticationPrincipal LoginDTO login
    ) {

        ClientItem item = ClientItem.builder()
                .clientId(clientId)
                .materialId(materialId)
                .unitPrice(unitPrice)
                .minOrderQty(moq)
                .unit(unit)
                .orderUnit(orderUnit)
                .leadDays(leadDays)
                .supplyAvailable(supplyAvailable)
                .createdBy(login.getEmpId())
                .build();

        itemRepository.save(item);

        // 🔥 저장 후 취급품목 탭으로 이동
        return "redirect:/sales/client/" 
        + clientId 
        + "?tab=item";
    }
    
    
    /* ======================================================
        10. 🔥 협력사 취급제품 수정 페이지
    ====================================================== */
    @GetMapping("/{clientId}/items/{itemId}/edit")
    public String itemEditPage(
            @PathVariable("clientId") String clientId,
            @PathVariable("itemId") Long itemId,
            Model model,
            RedirectAttributes rttr
    ) {
        ClientItem item = itemRepository.findById(itemId)
                .orElse(null);
        
        if (item == null) {
            rttr.addFlashAttribute("msg", "존재하지 않는 품목입니다.");
            return "redirect:/sales/client/" + clientId + "?tab=item";
        }

        Material material = materialRepository.findByMatCode(item.getMaterialId())
                .orElse(null);

        model.addAttribute("clientId", clientId);
        model.addAttribute("item", item);
        model.addAttribute("material", material);

        return "sales/supplier_item_edit";
    }


    /* ======================================================
        11. 🔥 협력사 취급제품 수정 프로세스
    ====================================================== */
    @PostMapping("/{clientId}/items/{itemId}/edit")
    public String itemEditProcess(
            @PathVariable("clientId") String clientId,
            @PathVariable("itemId") Long itemId,
            @RequestParam("unitPrice") BigDecimal unitPrice,
            @RequestParam("moq") BigDecimal moq,
            @RequestParam("supplyAvailable") String supplyAvailable,
            @RequestParam("unit") String unit,
            @RequestParam("orderUnit") BigDecimal orderUnit,
            @RequestParam("leadDays") BigDecimal leadDays,
            @AuthenticationPrincipal LoginDTO login,
            RedirectAttributes rttr
    ) {
        ClientItem item = itemRepository.findById(itemId)
                .orElse(null);

        if (item == null) {
            rttr.addFlashAttribute("msg", "존재하지 않는 품목입니다.");
            return "redirect:/sales/client/" + clientId + "?tab=item";
        }

        // 수정
        item.setUnitPrice(unitPrice);
        item.setMinOrderQty(moq);
        item.setSupplyAvailable(supplyAvailable);
        item.setUnit(unit);
        item.setOrderUnit(orderUnit);
        item.setLeadDays(leadDays);
        item.setUpdatedBy(login.getEmpId());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);

        rttr.addFlashAttribute("msg", "품목이 수정되었습니다.");
        return "redirect:/sales/client/" + clientId + "?tab=item";
    }


    /* ======================================================
        12. 🔥 협력사 취급제품 인라인 수정 (AG Grid용)
    ====================================================== */
    @PutMapping("/{clientId}/items/{itemId}/update")
    @ResponseBody
    public String updateItemInline(
            @PathVariable("clientId") String clientId,
            @PathVariable("itemId") Long itemId,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal LoginDTO login
    ) {
        ClientItem item = itemRepository.findById(itemId)
                .orElse(null);

        if (item == null) {
            throw new RuntimeException("존재하지 않는 품목입니다.");
        }

        // 값 업데이트
        if (payload.containsKey("unitPrice")) {
            item.setUnitPrice(new BigDecimal(payload.get("unitPrice").toString()));
        }
        if (payload.containsKey("moq")) {
            item.setMinOrderQty(new BigDecimal(payload.get("moq").toString()));
        }
        if (payload.containsKey("unit")) {
            item.setUnit(payload.get("unit").toString());
        }
        if (payload.containsKey("orderUnit")) {
            item.setOrderUnit(new BigDecimal(payload.get("orderUnit").toString()));
        }
        if (payload.containsKey("leadDays")) {
            item.setLeadDays(new BigDecimal(payload.get("leadDays").toString()));
        }
        if (payload.containsKey("supplyAvailable")) {
            item.setSupplyAvailable(payload.get("supplyAvailable").toString());
        }

        item.setUpdatedBy(login.getEmpId());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);

        return "OK";
    }


    /* ======================================================
        13. 🔥 협력사 취급제품 상태 변경 (활성/비활성)
    ====================================================== */
    @PutMapping("/{clientId}/items/{itemId}/status")
    @ResponseBody
    public String updateItemStatus(
            @PathVariable("clientId") String clientId,
            @PathVariable("itemId") Long itemId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal LoginDTO login
    ) {
        ClientItem item = itemRepository.findById(itemId)
                .orElse(null);

        if (item == null) {
            throw new RuntimeException("존재하지 않는 품목입니다.");
        }

        String newStatus = payload.get("supplyAvailable");
        item.setSupplyAvailable(newStatus);
        item.setUpdatedBy(login.getEmpId());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);

        return "OK";
    }


    /* ======================================================
        13. 🔥 협력사 취급제품 삭제 (선택 사항)
    ====================================================== */
    @DeleteMapping("/{clientId}/items/{itemId}")
    @ResponseBody
    public String deleteItem(
            @PathVariable("clientId") String clientId,
            @PathVariable("itemId") Long itemId,
            @AuthenticationPrincipal LoginDTO login
    ) {
        ClientItem item = itemRepository.findById(itemId)
                .orElse(null);

        if (item == null) {
            throw new RuntimeException("존재하지 않는 품목입니다.");
        }

        itemRepository.delete(item);

        return "OK";
    }
    
    
    /* ======================================================
        14. 고객사 정보 수정
    ====================================================== */
    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody Client client) {
        clientService.update(client);
        return "OK";
    }
}
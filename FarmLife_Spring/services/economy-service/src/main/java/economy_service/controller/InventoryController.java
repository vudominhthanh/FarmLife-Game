package controller;

import dto.InventoryResponse;
import service.EconomyInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final EconomyInventoryService inventoryService;

    @GetMapping("/me")
    public ResponseEntity<InventoryResponse> getMyInventory(@RequestHeader("X-Player-Id") UUID playerId) {
        return ResponseEntity.ok(inventoryService.getInventory(playerId));
    }
}

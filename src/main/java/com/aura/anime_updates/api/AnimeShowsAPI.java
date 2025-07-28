package com.aura.anime_updates.api;

import com.aura.anime_updates.services.GetAuraAmountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//adding this @annotation makes this class a REST API/rest controller
@RestController
public class AnimeShowsAPI {

    @Autowired
    GetAuraAmountService getAuraAmountService;

    //Get mapping to make a get endpoint.PostMapping for post etc...
    @PostMapping("/aungko")
    public ResponseEntity<Map<String, String>> akAura() {
        String aura = getAuraAmountService.getAungKoAura();
        return ResponseEntity.ok(Map.of("aura", aura));
    }

    @GetMapping("/ap")
    public ResponseEntity<Map<String, String>> apAura() {
        String aura = getAuraAmountService.getApAura();
        return ResponseEntity.ok(Map.of("aura", aura));
    }
}

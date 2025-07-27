package com.aura.anime_updates.api;

import com.aura.anime_updates.services.GetAuraAmountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//adding this @annotation makes this class a REST API/rest controller
@RestController
public class AnimeShowsAPI {

    @Autowired
    GetAuraAmountService getAuraAmountService;

    //Get mapping to make a get endpoint.PostMapping for post etc...
    @GetMapping("/aungko")
    public String akAura() {
        //here you will define what will happen when this endpoint is called and return the result
        //change json format. format json any way you like.
        //call services etc
        return getAuraAmountService.getAungKoAura();
    }

    @GetMapping("/ap")
    public String apAura() {
        return getAuraAmountService.getApAura();
    }
}

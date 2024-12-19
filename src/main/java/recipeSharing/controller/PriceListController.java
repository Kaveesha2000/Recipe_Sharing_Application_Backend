package recipeSharing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import recipeSharing.service.PriceListService;

import java.util.Map;


@RestController
public class PriceListController {

    @Autowired
    private PriceListService priceListService;

    @GetMapping("/prices")
    public Map<String, String> getPrices() {
        return priceListService.getItemPrices();
    }

    @PostMapping("/prices")
    public String updateItemPrice(@RequestBody Map<String, String> newPrices) {
        newPrices.forEach((item, price) -> priceListService.getItemPrices().put(item, price));
        return "Prices updated successfully!";
    }
}

package recipeSharing.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PriceListService {

    private Map<String, String> itemPrices = new HashMap<>();

    // ObjectMapper for reading the JSON file
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> getItemPrices() {
        return itemPrices;
    }

    // Load the prices from the JSON file on application startup
    @PostConstruct
    public void loadPricesFromJson() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prices.json")) {
            if (inputStream != null) {
                itemPrices = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
                System.out.println("Prices loaded from JSON: " + itemPrices);
            } else {
                System.err.println("Could not find the prices.json file!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update prices randomly every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void updatePrices() {
        Random random = new Random();

        itemPrices.replaceAll((item, price) -> {
            double newPrice = 100 + (500 - 100) * random.nextDouble(); // Random price between Rs. 100 and Rs. 500
            return String.format("Rs. %.2f", newPrice);
        });

        System.out.println("Prices updated: " + itemPrices);
    }
}

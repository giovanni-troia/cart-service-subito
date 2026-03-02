package it.subito.cart.seed;

import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the database with initial product data.
 * Runs on application startup.
 */
@Configuration
@Slf4j
public class ProductSeedConfig {

    @Bean
    public CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                log.info("Seeding database with initial products...");

                ProductEntity[] products = {
                    ProductEntity.builder()
                        .name("Premium Widget")
                        .description("High-quality widget with excellent durability")
                        .unitNetPrice(new BigDecimal("50.00"))
                        .vatRate(new BigDecimal("0.22"))
                        .currency("EUR")
                        .build(),

                    ProductEntity.builder()
                        .name("Standard Widget")
                        .description("Standard widget for everyday use")
                        .unitNetPrice(new BigDecimal("30.00"))
                        .vatRate(new BigDecimal("0.22"))
                        .currency("EUR")
                        .build(),

                    ProductEntity.builder()
                        .name("Economy Widget")
                        .description("Budget-friendly widget")
                        .unitNetPrice(new BigDecimal("15.50"))
                        .vatRate(new BigDecimal("0.22"))
                        .currency("EUR")
                        .build(),

                    ProductEntity.builder()
                        .name("Premium Service")
                        .description("Professional service package")
                        .unitNetPrice(new BigDecimal("100.00"))
                        .vatRate(new BigDecimal("0.10"))
                        .currency("EUR")
                        .build(),

                    ProductEntity.builder()
                        .name("Support Package")
                        .description("24/7 support and maintenance")
                        .unitNetPrice(new BigDecimal("25.75"))
                        .vatRate(new BigDecimal("0.05"))
                        .currency("EUR")
                        .build()
                };

                for (ProductEntity product : products) {
                    productRepository.save(product);
                    log.info("Seeded product: {} - {}", product.getId(), product.getName());
                }

                log.info("Database seeding completed");
            } else {
                log.info("Products already exist in database, skipping seed");
            }
        };
    }
}


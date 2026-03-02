package it.subito.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cart Service API")
                .version("1.0.0")
                .description("REST API for creating and managing orders with pricing and VAT calculations")
                .contact(new Contact()
                    .name("Application Author")
                    .email("giovannitroia1996@gmail.com")));
    }
}


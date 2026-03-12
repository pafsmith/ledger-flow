package dev.pafsmith.ledgerflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI ledgerFlowOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("LedgerFlow API")
            .description("Personal finance API for accounts, transactions, categories, and budgets.")
            .version("v1")
            .contact(new Contact()
                .name("Paul Smith")
                .email("paul@pafsmith.dev"))
            .license(new License()
                .name("MIT")));
  }
}

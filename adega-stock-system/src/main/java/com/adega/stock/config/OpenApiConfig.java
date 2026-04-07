package com.adega.stock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Adega Stock System API")
                        .description("Sistema de controle de estoque para adega. Gerencie produtos, categorias, combos, vendas e movimentacoes de estoque.")
                        .version("1.0.0"));
    }
}

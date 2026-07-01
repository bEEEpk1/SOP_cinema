package edu.rutmiit.demo.cinemaapicontract.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Cinema API",
                version = "1.0.0",
                description = """
                        Contract-first REST API для core-сервиса системы бронирования билетов кинотеатра.
                        Контракт описывает пользователей, фильмы, залы, места, сеансы, бронирования,
                        билеты и лист ожидания в стиле, заданном лекцией и лабораторной работой.
                        """,
                contact = @Contact(name = "Cinema Core Team")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development")
        }
)
@SecurityScheme(
        name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT Bearer token"
)
public final class CinemaApiContractConfig {

    public static final String SECURITY_SCHEME_BEARER = "bearerAuth";

    private CinemaApiContractConfig() {
    }
}

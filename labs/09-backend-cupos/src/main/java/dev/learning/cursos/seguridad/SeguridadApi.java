package dev.learning.cursos.seguridad;

import jakarta.servlet.DispatcherType;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IdentidadesAlumnos.class)
class SeguridadApi {
    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/cursos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/metrics", "/actuator/metrics/*")
                        .hasAuthority("SCOPE_observabilidad:leer")
                        .requestMatchers(HttpMethod.POST, "/cursos/{cursoId}/inscripciones")
                        .hasAuthority("SCOPE_inscripciones:crear")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(IdentidadesAlumnos config, ResourceLoader recursos) throws IOException {
        if (!StringUtils.hasText(config.clavePublica())) {
            return token -> { throw new BadJwtException("El emisor de tokens no esta configurado"); };
        }
        if (!StringUtils.hasText(config.emisor()) || !StringUtils.hasText(config.audiencia())) {
            throw new IllegalArgumentException("La clave JWT requiere emisor y audiencia");
        }
        RSAPublicKey clave;
        try (var entrada = recursos.getResource(config.clavePublica()).getInputStream()) {
            clave = RsaKeyConverters.x509().convert(entrada);
        }
        var decoder = NimbusJwtDecoder.withPublicKey(clave).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(config.emisor()),
                new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(config.audiencia())),
                new JwtClaimValidator<Object>("exp", exp -> exp != null),
                new JwtClaimValidator<String>("sub", StringUtils::hasText)));
        return decoder;
    }
}

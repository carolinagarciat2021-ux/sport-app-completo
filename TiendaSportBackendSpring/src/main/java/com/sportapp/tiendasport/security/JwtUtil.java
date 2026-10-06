package com.sportapp.tiendasport.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Genera y valida los tokens JWT que reemplazan el sistema de tokens en
 * memoria del backend anterior. La ventaja de JWT firmado: el propio token
 * ya contiene la información (quién es, qué rol tiene) y una firma que
 * demuestra que el servidor lo emitió; no depende de un mapa en memoria que
 * se pierde si el servidor se reinicia.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey obtenerLlave() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Integer idClientes, String correo, Integer idRol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id_clientes", idClientes);
        claims.put("id_rol", idRol);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(correo)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(obtenerLlave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public Integer extraerIdClientes(String token) {
        Claims claims = extraerTodosLosClaims(token);
        return claims.get("id_clientes", Integer.class);
    }

    public Integer extraerIdRol(String token) {
        Claims claims = extraerTodosLosClaims(token);
        return claims.get("id_rol", Integer.class);
    }

    public boolean tokenValido(String token, String correoEsperado) {
        try {
            String correoDelToken = extraerCorreo(token);
            return correoDelToken.equals(correoEsperado) && !tokenExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerTodosLosClaims(token));
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(obtenerLlave())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

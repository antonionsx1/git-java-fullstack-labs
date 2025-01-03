package org.iadb.lmw.utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.JwtPayloadDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtUtil {

	@Value("${security.jwt.key}")
	private String key;

	public String generateJwt(JwtPayloadDto payload) throws JWTCreationException {

		ObjectMapper mapper = new ObjectMapper();
		
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date expiration = calendar.getTime();

		try {
			String jsonInString = mapper.writeValueAsString(payload);
			Algorithm algorithm = Algorithm.HMAC256(key);
			return JWT.create().withIssuer("auth0").withIssuedAt(now).withExpiresAt(expiration)
					.withSubject(jsonInString).sign(algorithm);
		} catch (Exception e) {
			throw new JWTCreationException("Parece que algo anda mal", new Throwable("Esto está mal"));
		}
	}

	public JwtPayloadDto validateToken(String bearerToken) throws AuthenticationException {
		if (bearerToken == null || bearerToken.equals("") || !bearerToken.contains(" ")) {
			throw new AuthenticationException("Malformed JWT");
		}
		String token = bearerToken.split(" ")[1];

		ObjectMapper mapper = new ObjectMapper();

		try {
			Algorithm algorithm = Algorithm.HMAC256(key);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
			DecodedJWT jwt = verifier.verify(token);
			String json = jwt.getSubject();
			return mapper.readValue(json, JwtPayloadDto.class);
		} catch (JWTVerificationException e0) {
			throw new AuthenticationException(e0.getMessage());
		} catch (IllegalArgumentException e1) {
			throw new AuthenticationException(e1.getMessage());
		} catch (IOException e2) {
			throw new AuthenticationException(e2.getMessage());
		}
	}
	
}

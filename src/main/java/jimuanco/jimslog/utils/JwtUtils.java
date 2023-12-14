package jimuanco.jimslog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jimuanco.jimslog.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private final String SECRET_KEY;
    private static final String ROLE_KEY = "role";
    private static final long ACCESS_TIME = 30 * 60 * 1000L;

    public JwtUtils(@Value("${jwt.secret.key}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(ROLE_KEY, user.getRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TIME))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (SignatureException e) {
            log.error("잘못된 JWT 서명입니다.");

        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰입니다.");

        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");

        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");

        } catch (IllegalArgumentException e) {
            log.error("유효하지 않는 JWT 토큰입니다.");

        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return extractAllClaims(token).get(ROLE_KEY).toString();
    }

    private Claims extractAllClaims(String token) {
        return verifyToken(token).getPayload();
    }

    private Jws<Claims> verifyToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

package com.softeem.rzb.base.util;


import com.softeem.rzb.common.exception.BusinessException;
import com.softeem.rzb.common.result.ResponseEnum;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

public class JwtUtils {

    private static long tokenExpiration = 24*60*60*1000;
    private static String tokenSignKey = "softeem123456";

    private static Key getKeyInstance(){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] bytes = DatatypeConverter.parseBase64Binary(tokenSignKey);
        return new SecretKeySpec(bytes,signatureAlgorithm.getJcaName());
    }

    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("SRB-USER")//主题
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))//过期时间
                .claim("userId", userId)//放入用户ID
                .claim("userName", userName)//放入用户昵称
                .signWith(SignatureAlgorithm.HS512, getKeyInstance())//签名算法和密钥
                .compressWith(CompressionCodecs.GZIP)//压缩
                .compact();
        return token;
    }

    /**
     * 判断token是否有效
     * @param token
     * @return
     */
    public static boolean checkToken(String token) {
        if(StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Long getUserId(String token) {
        Claims claims = getClaims(token);
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    public static String getUserName(String token) {
        Claims claims = getClaims(token);
        return (String)claims.get("userName");
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }

    /**
     * 校验token并返回Claims
     * @param token
     * @return
     */
    private static Claims getClaims(String token) {
        if(StringUtils.isEmpty(token)) {
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return claims;
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}


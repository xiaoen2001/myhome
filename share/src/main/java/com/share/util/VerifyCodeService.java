package com.share.util;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerifyCodeService {

    // key: email, value: 验证码 (实际生产应存储更多信息，如过期时间)
    private final Map<String, String> codeMap = new ConcurrentHashMap<>();

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000; // 5分钟
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    public void saveCode(String email, String code) {
        codeMap.put(email, code);
        expireMap.put(email, System.currentTimeMillis() + EXPIRE_MILLIS);
    }

    public boolean verifyCode(String email, String code) {
        Long expireTime = expireMap.get(email);
        if (expireTime == null || System.currentTimeMillis() > expireTime) {
            return false;
        }
        String savedCode = codeMap.get(email);
        return savedCode != null && savedCode.equals(code);
    }

    public void removeCode(String email) {
        codeMap.remove(email);
        expireMap.remove(email);
    }
}
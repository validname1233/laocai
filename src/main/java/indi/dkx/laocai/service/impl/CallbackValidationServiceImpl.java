package indi.dkx.laocai.service.impl;

import indi.dkx.laocai.config.QQBotConfig;
import indi.dkx.laocai.pojo.CallbackValidationRequest;
import indi.dkx.laocai.pojo.CallbackValidationResponse;
import indi.dkx.laocai.service.CallbackValidationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.security.KeyFactory;
import java.security.PrivateKey;

import java.security.Signature;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.HexFormat;

@Slf4j
@Service
public class CallbackValidationServiceImpl implements CallbackValidationService {

    @Resource
    private QQBotConfig qqBotConfig;

    @Override
    public CallbackValidationResponse verify(CallbackValidationRequest request) {
        String seed = qqBotConfig.getSecret();
        while (seed.getBytes(StandardCharsets.UTF_8).length < 32) seed += seed;
        byte[] seedBytes = Arrays.copyOf(seed.getBytes(StandardCharsets.UTF_8), 32);

        // 创建Ed25519参数规范
        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        // 创建私钥规范
        EdECPrivateKeySpec privateKeySpec = new EdECPrivateKeySpec(paramSpec, seedBytes);

        try {
            // 获取密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            // 生成私钥
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            // 签名
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);

            String msg = request.getEvent_ts() + request.getPlain_token();
            signature.update(msg.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = signature.sign();

            return new CallbackValidationResponse(request.getPlain_token(), HexFormat.of().formatHex(signatureBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

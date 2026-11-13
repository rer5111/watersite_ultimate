package com.waltercraft.payment.client.yoomoney;

import com.waltercraft.payment.config.YoomoneyProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YoomoneySignatureVerifierTest {

    @Test
    void verifiesCorrectSha1Signature() throws Exception {
        String secret = "my_secret_word";
        YoomoneyProperties properties = new YoomoneyProperties("410011234567890", secret, "http://localhost", null);
        YoomoneySignatureVerifier verifier = new YoomoneySignatureVerifier(properties);

        String notificationType = "p2p-incoming";
        String operationId = "987654321";
        String amount = "291.00";
        String currency = "643";
        String datetime = "2026-11-13T17:00:00Z";
        String sender = "410019999999";
        String codepro = "false";
        String label = "d872f238-d656-4cf3-bca9-59eb4e64f849";

        String toSign = String.join("&",
                notificationType, operationId, amount, currency, datetime, sender, codepro, secret, label
        );
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String hash = HexFormat.of().formatHex(md.digest(toSign.getBytes(StandardCharsets.UTF_8)));

        assertTrue(verifier.verify(
                notificationType, operationId, amount, currency, datetime, sender, codepro, label, hash
        ));

        assertFalse(verifier.verify(
                notificationType, operationId, amount, currency, datetime, sender, codepro, label, "wrong_hash"
        ));
    }

    @Test
    void verifiesHmacSha256SignatureAccordingToYooMoneyDoc() {
        String secret = "secret123";
        YoomoneyProperties properties = new YoomoneyProperties("410011234567890", secret, "http://localhost", null);
        YoomoneySignatureVerifier verifier = new YoomoneySignatureVerifier(properties);

        Map<String, String> params = new HashMap<>();
        params.put("notification_type", "p2p-incoming");
        params.put("operation_id", "904035776918098009");
        params.put("amount", "0.99");
        params.put("withdraw_amount", "1.00");
        params.put("currency", "643");
        params.put("datetime", "2014-04-28T16:31:28Z");
        params.put("sender", "41003188981230");
        params.put("codepro", "false");
        params.put("label", "YM.label.12345");
        params.put("test_notification", "false");
        params.put("unaccepted", "false");
        params.put("lastname", "{% translate %}Иванов{% /translate %}");
        params.put("firstname", "{% translate %}Иван{% /translate %}");
        params.put("fathersname", "{% translate %}Иванович{% /translate %}");
        params.put("email", "address@example.ru");
        params.put("phone", "+79253332211");
        params.put("city", "{% translate %}Москва{% /translate %}");
        params.put("street", "{% translate %}Тверская{% /translate %}");
        params.put("building", "12");
        params.put("suite", "10");
        params.put("flat", "10");
        params.put("zip", "125075");
        params.put("sha1_hash", "8693ddf402fe5dcc4c4744d466cabada2628148c");
        params.put("sign", "092318d12a1249b8ff5cb7b93e1b409a35bfe01eadee9525a147dc977b4eb056");

        boolean ok = verifier.verify(params);
        if (!ok) {
            System.err.println("FAILED VERIFICATION");
        }
        assertTrue(ok);

        params.put("sign", "bad_signature");
        params.remove("sha1_hash");
        assertFalse(verifier.verify(params));
    }
}

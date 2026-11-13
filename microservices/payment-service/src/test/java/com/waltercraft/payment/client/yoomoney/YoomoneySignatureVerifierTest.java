package com.waltercraft.payment.client.yoomoney;

import com.waltercraft.payment.config.YoomoneyProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

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
}

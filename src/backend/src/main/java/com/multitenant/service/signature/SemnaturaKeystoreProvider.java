package com.multitenant.service.signature;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

/**
 * Furnizează cheia privată și lanțul de certificate folosite pentru semnarea electronică
 * a contractelor. Dacă la calea configurată nu există deja un keystore, se generează
 * automat un certificat auto-semnat (self-signed) pentru mediul de dezvoltare/demo.
 *
 * ATENȚIE: un certificat auto-semnat NU constituie o semnătură electronică calificată
 * eIDAS. Pentru producție, {@code app.signature.keystore-path} trebuie să indice un
 * keystore PKCS12 emis de un furnizor acreditat (ex. certSIGN, DigiSign).
 */
@Component
public class SemnaturaKeystoreProvider {

    private static final Logger log = LoggerFactory.getLogger(SemnaturaKeystoreProvider.class);
    private static final String KEYSTORE_TYPE = "PKCS12";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Value("${app.signature.keystore-path}")
    private String keystorePath;

    @Value("${app.signature.keystore-password}")
    private String keystorePassword;

    @Value("${app.signature.key-alias}")
    private String keyAlias;

    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    @PostConstruct
    public void init() throws Exception {
        Path path = Path.of(keystorePath);
        if (!Files.exists(path)) {
            log.warn("Nu a fost găsit niciun keystore de semnătură la '{}'. Se generează un certificat " +
                    "auto-semnat pentru uz de dezvoltare/demo. Pentru producție, configurați un certificat " +
                    "calificat eIDAS emis de un furnizor acreditat.", keystorePath);
            generateSelfSignedKeystore(path);
        }

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (var in = Files.newInputStream(path)) {
            keyStore.load(in, keystorePassword.toCharArray());
        }

        privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
        certificateChain = keyStore.getCertificateChain(keyAlias);

        if (privateKey == null || certificateChain == null) {
            throw new IllegalStateException("Alias-ul '" + keyAlias + "' nu a fost găsit în keystore-ul de semnătură");
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }

    private void generateSelfSignedKeystore(Path path) throws Exception {
        Files.createDirectories(path.getParent());

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name subject = new X500Name("CN=Registru Agricol, O=Primarie, C=RO");
        BigInteger serial = BigInteger.valueOf(Instant.now().toEpochMilli());
        Date notBefore = Date.from(Instant.now());
        Date notAfter = Date.from(Instant.now().plusSeconds(10L * 365 * 24 * 3600));

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPair.getPrivate());

        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        keyStore.setKeyEntry(keyAlias, keyPair.getPrivate(), keystorePassword.toCharArray(),
                new Certificate[]{certificate});

        try (var out = Files.newOutputStream(path)) {
            keyStore.store(out, keystorePassword.toCharArray());
        }
    }
}

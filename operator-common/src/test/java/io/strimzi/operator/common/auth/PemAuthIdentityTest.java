/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.common.auth;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.strimzi.api.kafka.model.kafka.KafkaResources;
import io.strimzi.operator.common.operator.MockCertManager;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PemAuthIdentityTest {
    public static final String NAMESPACE = "testns";
    public static final String CLUSTER = "testcluster";

    @Test
    public void testSecretWithMissingKeyThrowsException() {
        Secret secretWithMissingClusterOperatorKey = new SecretBuilder()
                .withNewMetadata()
                    .withName(KafkaResources.clusterOperatorCertsSecretName(CLUSTER))
                    .withNamespace(NAMESPACE)
                .endMetadata()
                .withData(emptyMap())
                .build();
        Exception e = assertThrows(RuntimeException.class, () -> PemAuthIdentity.clusterOperator(secretWithMissingClusterOperatorKey));
        assertThat(e.getMessage(), is("The Secret testns/testcluster-cluster-operator-certs is missing the field cluster-operator.key"));
    }

    @Test
    public void testSecretWithMissingCertChainThrowsException() {
        Secret secretWithMissingClusterOperatorKey = new SecretBuilder()
                .withNewMetadata()
                    .withName(KafkaResources.clusterOperatorCertsSecretName(CLUSTER))
                    .withNamespace(NAMESPACE)
                .endMetadata()
                .withData(Map.of("cluster-operator.key", "key"))
                .build();
        Exception e = assertThrows(RuntimeException.class, () -> PemAuthIdentity.clusterOperator(secretWithMissingClusterOperatorKey));
        assertThat(e.getMessage(), is("The Secret testns/testcluster-cluster-operator-certs is missing the field cluster-operator.crt"));
    }

    @Test
    public void testSecretCorrupted() {
        Secret secretWithBadCertificate = new SecretBuilder()
                .withNewMetadata()
                    .withName(KafkaResources.clusterOperatorCertsSecretName(CLUSTER))
                    .withNamespace(NAMESPACE)
                .endMetadata()
                .withData(Map.of("cluster-operator.key", MockCertManager.clusterCaKey(),
                        "cluster-operator.crt", "bm90YWNlcnQ=", //notacert
                        "cluster-operator.p12", "bm90YXRydXN0c3RvcmU=", //notatruststore
                        "cluster-operator.password", "bm90YXBhc3N3b3Jk")) //notapassword
                .build();
        PemAuthIdentity pemAuthIdentity = PemAuthIdentity.clusterOperator(secretWithBadCertificate);
        Exception e = assertThrows(RuntimeException.class, () -> pemAuthIdentity.jksKeyStore(new char[]{}));
        assertThat(e.getMessage(), is("Bad/corrupt certificate found in data.cluster-operator.crt of Secret testcluster-cluster-operator-certs in namespace testns"));
    }

    @Test
    public void testMissingSecret() {
        Exception e = assertThrows(NullPointerException.class, () -> PemAuthIdentity.clusterOperator(null));
        assertThat(e.getMessage(), is("Cannot extract auth identity from null secret."));
    }
}

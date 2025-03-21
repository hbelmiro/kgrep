package com.thegreatapi.kgrep.serviceaccount;

import com.thegreatapi.kgrep.KubernetesTestsUtil;
import com.thegreatapi.kgrep.resource.GenericResourceGrepper;
import com.thegreatapi.kgrep.resource.GenericResourceRetriever;
import com.thegreatapi.kgrep.resource.ResourceLine;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@WithKubernetesTestServer
@QuarkusTest
class ServiceAccountsCommandTest {

    private static final String NAMESPACE = "kubeflow";

    private static final String KIND = "ServiceAccount";

    private final KubernetesClient client;

    private final ServiceAccountsCommand command;

    @Inject
    ServiceAccountsCommandTest(KubernetesClient client, GenericResourceRetriever retriever, GenericResourceGrepper grepper) {
        this.client = client;
        this.command = new ServiceAccountsCommand(retriever, grepper);
    }

    @Test
    void grep() {
        createServiceAccount();

        await().atMost(20, TimeUnit.SECONDS)
                .until(() -> command.getOccurrences(KIND, NAMESPACE, "kubeflow").size() == 3);

        assertThat(command.getOccurrences(KIND, NAMESPACE, "kubeflow"))
                .containsExactlyInAnyOrder(
                        new ResourceLine(KIND + "/pipeline-runner", 8, "      :\\\"kubeflow-pipelines\\\"},\\\"name\\\":\\\"pipeline-runner\\\",\\\"namespace\\\":\\\"kubeflow\\\"\\"),
                        new ResourceLine(KIND + "/pipeline-runner", 13, "    application-crd-id: \"kubeflow-pipelines\""),
                        new ResourceLine(KIND + "/pipeline-runner", 15, "  namespace: \"kubeflow\"")
                );
    }

    private void createServiceAccount() {
        InputStream stream = KubernetesTestsUtil.getResourceAsStream("serviceaccount.yaml");
        ServiceAccount serviceAccount = client.serviceAccounts().load(stream).item();

        client.serviceAccounts().resource(serviceAccount).create();
    }
}
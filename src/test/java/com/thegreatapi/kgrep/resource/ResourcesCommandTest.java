package com.thegreatapi.kgrep.resource;

import com.thegreatapi.kgrep.KubernetesTestsUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@WithKubernetesTestServer
@QuarkusTest
class ResourcesCommandTest {

    private static final String NAMESPACE = "kubeflow";

    private static final String KIND = "pod";

    private final KubernetesClient client;

    private final ResourcesCommand command;

    @BeforeEach
    void setup() {
        createPods();
    }

    @AfterEach
    void tearDown() {
        deletePods();
    }

    @Inject
    ResourcesCommandTest(KubernetesClient client, GenericResourceRetriever retriever, GenericResourceGrepper grepper) {
        this.client = client;
        this.command = new ResourcesCommand(retriever, grepper);
    }

    @Test
    void grep() {
        await().atMost(20, TimeUnit.SECONDS)
                .until(() -> command.getOccurrences(NAMESPACE, "kubeflow", "v1", KIND).size() == 9);

        assertThat(command.getOccurrences(NAMESPACE, "kubeflow", "v1", KIND))
                .containsExactlyInAnyOrder(
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 20, "  namespace: \"kubeflow\""),
                        new ResourceLine(KIND + "/mariadb-sample-5bd78c456f-kffct", 20, "  namespace: \"kubeflow\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 34, "      value: \"kubeflow\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 45, "      value: \"mariadb-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 79, "      value: \"minio-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 87, "      value: \"ds-pipeline-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 101, "      value: \"http://minio-sample.kubeflow.svc.cluster.local:9000\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 194, "      kubeflow\\\"}}\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 195, "    - \"--openshift-sar={\\\"namespace\\\":\\\"kubeflow\\\",\\\"resource\\\":\\\"routes\\\",\\\"resourceName\\\"\\")
                );
    }

    @Test
    void grepNoNamespace() {
        client.getConfiguration().setNamespace(NAMESPACE);

        await().atMost(20, TimeUnit.SECONDS)
                .until(() -> command.getOccurrences("kubeflow", "v1", KIND).size() == 7);

        assertThat(command.getOccurrences("kubeflow", "v1", KIND))
                .containsExactlyInAnyOrder(
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 34, "      value: \"kubeflow\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 45, "      value: \"mariadb-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 79, "      value: \"minio-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 87, "      value: \"ds-pipeline-sample.kubeflow.svc.cluster.local\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 101, "      value: \"http://minio-sample.kubeflow.svc.cluster.local:9000\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 194, "      kubeflow\\\"}}\""),
                        new ResourceLine(KIND + "/ds-pipeline-sample-7b59bd7cb4-szxqb", 195, "    - \"--openshift-sar={\\\"namespace\\\":\\\"kubeflow\\\",\\\"resource\\\":\\\"routes\\\",\\\"resourceName\\\"\\")
                );
    }

    private void createPods() {
        createPod("pod1.yaml");
        createPod("pod2.yaml");
        createPod("test-namespace-pod1.yaml");
        createPod("test-namespace-pod2.yaml");
    }

    private void deletePods() {
        deletePod("pod1.yaml");
        deletePod("pod2.yaml");
        deletePod("test-namespace-pod1.yaml");
        deletePod("test-namespace-pod2.yaml");
    }

    private void createPod(String yamlOrJson) {
        InputStream stream = KubernetesTestsUtil.getResourceAsStream(yamlOrJson);
        Pod pod = client.pods().load(stream).item();

        client.pods().resource(pod).create();
    }

    private void deletePod(String yamlOrJson) {
        InputStream stream = KubernetesTestsUtil.getResourceAsStream(yamlOrJson);
        Pod pod = client.pods().load(stream).item();

        client.pods().resource(pod).delete();
    }
}

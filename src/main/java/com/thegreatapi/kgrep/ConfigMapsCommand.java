package com.thegreatapi.kgrep;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "configmaps", mixinStandardHelpOptions = true)
class ConfigMapsCommand implements Runnable {

    private static final String ANSI_RESET = "\u001B[0m";

    private static final String BLUE = "\033[0;34m";

    @CommandLine.Option(names = {"-n", "--namespace"}, description = "The Kubernetes namespace", required = true)
    private String namespace;

    @CommandLine.Option(names = {"-p", "--pattern"}, description = "grep search pattern", required = true)
    private String pattern;

    private final ConfigMapGrepper configMapGrepper;

    @SuppressWarnings("unused")
    @Inject
    ConfigMapsCommand(ConfigMapGrepper configMapGrepper) {
        this.configMapGrepper = configMapGrepper;
    }

    @Override
    public void run() {
        try {
            List<ResourceLine> occurrences = configMapGrepper.grep(namespace, pattern);
            occurrences.forEach(ConfigMapsCommand::print);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void print(ResourceLine resourceLine) {
        System.out.println(BLUE + resourceLine.resourceName() + ANSI_RESET + ": " + resourceLine.line());
    }
}

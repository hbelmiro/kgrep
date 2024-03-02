package com.thegreatapi.kgrep;

import com.thegreatapi.kgrep.configmap.ConfigMapsCommand;
import com.thegreatapi.kgrep.log.LogsCommand;
import com.thegreatapi.kgrep.pod.PodsCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "kgrep", mixinStandardHelpOptions = true, subcommands = {
        LogsCommand.class,
        ConfigMapsCommand.class,
        PodsCommand.class
})
class KgrepCommand {
}

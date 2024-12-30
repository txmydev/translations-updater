package com.github.txmy.translations.executor;

import com.github.txmy.translations.Settings;
import com.github.txmy.translations.utils.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileTransferExecutor implements IExecutor<Map<String, Boolean>> {

    private final Path pluginsFolderPath;
    private final Settings settings;

    private Map<String, String> files;

    public FileTransferExecutor(Path pluginsFolderPath, Settings settings) {
        this.pluginsFolderPath = pluginsFolderPath;
        this.settings = settings;
    }

    public FileTransferExecutor withFiles(Map<String, String> files) {
        this.files = files;
        return this;
    }

    private PhaseResult<Void> changeSingle(Map.Entry<String, String> entry) {
        final String subPath = entry.getKey();
        final String contents = entry.getValue();
        final Path path = pluginsFolderPath.resolve(subPath);

        PhaseResult<Void> result = new PhaseResult<>();
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());

                Files.createFile(path);
                Files.writeString(path, contents, StandardOpenOption.WRITE);
            } else {
                if (settings.createCopiesOfOldFiles()) {
                    // TODO: Do this shit
                }
                Files.writeString(path, contents, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            result.result = Result.SUCCESS;
        } catch (IOException ex) {
            result.result = Result.FAILED;
            result.thrown = ex;
        }

        return result;
    }

    @Override
    public Map<String, Boolean> execute() {
        Map<String, Boolean> map = new HashMap<>();
        files.entrySet().forEach(entry -> {
            PhaseResult<Void> result = changeSingle(entry);
            if (result.hasFailed()) {
                error(result, "updating file " + entry.getKey());
            }

            map.put(entry.getKey(), !result.hasFailed());
        });

        return map;
    }

    private void error(PhaseResult<?> result, String phaseDescription) {
        String err;
        if (result.thrown != null) {
            err = result.thrown.getLocalizedMessage();
        } else {
            err = "unknown";
        }

        LogUtils.error(String.format("error: catched error while fetching %s of the repository: %s", phaseDescription, err));
    }

}

package com.github.txmy.translations;

import java.util.Map;

public interface TranslationsUpdaterService {

    GitHubUpdaterExecutor getGitHubUpdaterExecutor();
    FileTransferExecutor getFileTransferExecutor();

    Map<String, Boolean> update();

}

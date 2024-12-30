package com.github.txmy.translations.service;

import com.github.txmy.translations.executor.FileTransferExecutor;
import com.github.txmy.translations.executor.GitHubUpdaterExecutor;

import java.util.Map;

public interface TranslationsUpdaterService {

    GitHubUpdaterExecutor getGitHubUpdaterExecutor();
    FileTransferExecutor getFileTransferExecutor();

    Map<String, Boolean> update(boolean force);

}

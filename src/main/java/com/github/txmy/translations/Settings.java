package com.github.txmy.translations;

import java.util.List;

public record Settings(boolean runOnStartup, boolean createCopiesOfOldFiles, List<String> commandsAfterUpdate) {
}

package com.github.txmy.translations.executor;

import com.github.txmy.translations.Credentials;
import com.github.txmy.translations.utils.HttpUtils;
import com.github.txmy.translations.utils.LogUtils;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class GitHubUpdaterExecutor implements IExecutor<Map<String, String>> {

    private static final boolean DEBUG = false;

    public static final String BASE_URL = "https://api.github.com/repos/";

    private final Credentials credentials;

    public GitHubUpdaterExecutor(Credentials credentials) {
        this.credentials = credentials;
    }

    private PhaseResult<JsonObject> fetchDirectories() {
        PhaseResult<JsonObject> result = new PhaseResult<>();
        try {
            String endpoint = BASE_URL + credentials.repository() + "/contents";
            JsonArray array = HttpUtils.getJsonArray(credentials, endpoint);
            JsonObject resultObject = new JsonObject();

            // Grab all the directories tree links, maybe
            array.forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                if (object.has("type")) {
                    String fileType = object.get("type").getAsString();
                    if (!fileType.equals("dir")) {
                        return;
                    }

                    if (!object.has("_links")) {
                        return;
                    }

                    JsonObject linksObject = object.get("_links").getAsJsonObject();
                    if (!linksObject.has("git")) {
                        return;
                    }

                    String treeLink = linksObject.get("git").getAsString();
                    resultObject.addProperty(object.get("name").getAsString(), treeLink);
                }
            });

            result.object = resultObject;
            result.result = Result.SUCCESS;

            if (DEBUG) {
                LogUtils.log(String.format("repos: found %d available repositories", resultObject.entrySet().size()));
            }
        } catch (IOException exception) {
            result.result = Result.FAILED;
            result.object = new JsonObject();
            result.thrown = exception;
        }

        return result;
    }

    private PhaseResult<JsonObject> goThroughTree(String treeLink) {
        PhaseResult<JsonObject> result = new PhaseResult<>();
        try {
            JsonObject object = HttpUtils.getJsonObject(credentials, treeLink);
            if (!object.has("tree")) {
                result.result = Result.FAILED;
                result.object = object;
                return result;
            }

            JsonObject resultObject = new JsonObject();

            JsonArray treeArray = object.get("tree").getAsJsonArray();
            treeArray.forEach(element -> {
                JsonObject fileObject = element.getAsJsonObject();
                boolean isSubDirectory = fileObject.has("type") && "tree".equals(fileObject.get("type").getAsString());
                if (fileObject.has("path") && fileObject.has("url")) {
                    resultObject.addProperty((isSubDirectory ? "$subdir_" : "") + fileObject.get("path").getAsString(), fileObject.get("url").getAsString());
                }
            });

            result.object = resultObject;
            result.result = Result.SUCCESS;
        } catch (IOException e) {
            result.result = Result.FAILED;
            result.object = new JsonObject();
            result.thrown = e;
        }

        return result;
    }

    private PhaseResult<String> fetchFileContents(String fileUrl) {
        PhaseResult<String> result = new PhaseResult<>();

        try {
            JsonObject object = HttpUtils.getJsonObject(credentials, fileUrl);
            if (!object.has("content")) {
                result.result = Result.FAILED;
                result.object = object.toString();
                return result;
            }

            String rawBase64 = object.get("content").getAsString().replaceAll("\\s", "");
            String decoded = new String(Base64.getDecoder().decode(rawBase64.getBytes(StandardCharsets.UTF_8)));

            result.result = Result.SUCCESS;
            result.object = decoded;
        } catch (IOException e) {
            result.result = Result.FAILED;
            result.object = "";
            result.thrown = e;
        } catch (Exception e) {
            result.result = Result.FAILED;
            result.object = e.getLocalizedMessage();

            LogUtils.error("error while decoding base64: " + e.getLocalizedMessage());
        }

        return result;
    }


    @Override
    public Map<String, String> execute() {
        Map<String, String> map = Maps.newHashMap();
        PhaseResult<JsonObject> fetchDirectoriesResult = fetchDirectories();
        if (fetchDirectoriesResult.hasFailed()) {
            error(fetchDirectoriesResult, "fetching github directories");
            return Maps.newHashMap();
        }

        fetchDirectoriesResult.object.entrySet().forEach(entry -> {
            String mainFolder = entry.getKey();
            // check whether the plugin is on the server or not.
            if (Bukkit.getPluginManager().getPlugin(mainFolder) == null) {
                LogUtils.log(mainFolder + " is not on the server, skipping its language files.");
                return;
            }

            String treeLink = entry.getValue().getAsString();
            PhaseResult<JsonObject> folderLinksResult = goThroughTree(treeLink);
            if (folderLinksResult.hasFailed()) {
                error(folderLinksResult, "grabbing " + mainFolder + " folder links");
                return;
            }

            JsonObject folderLinks = folderLinksResult.object;
            processFolderLinks(map, mainFolder, folderLinks);
        });

        return map;
    }


    private void processFolderLinks(Map<String, String> map, String mainFolder, JsonObject folderLinks) {
        folderLinks.entrySet().forEach(entry -> {
            String path = entry.getKey();

            if (path.startsWith("$subdir_")) {
                String subDirectoryName = path.replaceFirst("\\$subdir_", "");
                PhaseResult<JsonObject> subDirectoryFiles = goThroughTree(entry.getValue().getAsString());
                if (subDirectoryFiles.hasFailed()) {
                    error(subDirectoryFiles, "going through sub-directory " + mainFolder + "/" + subDirectoryName);
                    return;
                }

                processFolderLinks(map, mainFolder + "/" + subDirectoryName, subDirectoryFiles.object);
                return;
            }

            String fileLink = entry.getValue().getAsString();

            PhaseResult<String> result = fetchFileContents(fileLink);
            if (result.hasFailed()) {
                error(result, "fetching " + path + " contents");
                return;
            }

            map.put(mainFolder + "/" + path, result.object);
        });
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

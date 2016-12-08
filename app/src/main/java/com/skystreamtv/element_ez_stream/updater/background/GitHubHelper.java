package com.skystreamtv.element_ez_stream.updater.background;

import android.util.Log;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class GitHubHelper {

    private static final String REPOSITORY = "skystreamtv/updater";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static GHRepository repository;

    public static GHRepository connectRepository() throws IOException {
        if (repository == null) {
            Log.d("PlayerUpdater", "Connect to GitHub");
            GitHub github;
            Log.d("PlayerUpdater", "Try to authenticate to GitHub or go anonymously");
            if (USERNAME.equals("") || PASSWORD.equals(""))
                github = GitHub.connectAnonymously();
            else
                github = GitHub.connectUsingPassword(USERNAME, PASSWORD);
            Log.d("PlayerUpdater", "Getting repository " + REPOSITORY + "...");
            try {
                repository = github.getRepository(REPOSITORY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("PlayerUpdater", "Got it");
        }
        return repository;
    }

    interface GitHubTask {
        void contextDestroyed();
    }

    public interface GitHubCallbacks<GitHubResult> {
        void onCancelled(String reason);
        void onPostExecute(GitHubResult result);
    }
}

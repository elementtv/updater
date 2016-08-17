package com.skystreamtv.element_ez_stream.updater.background;

import android.content.res.Resources;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.R;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class GitHubHelper {

    private static GHRepository repository;

    public static GHRepository connectRepository(Resources resources) throws IOException {
        if (repository == null) {
            Log.d("PlayerUpdater", "Connect to GitHub");
            String repository_name = resources.getString(R.string.github_repository);
            String username = resources.getString(R.string.github_username);
            String password = resources.getString(R.string.github_password);
            GitHub github;
            Log.d("PlayerUpdater", "Try to authenticate to GitHub or go anonymously");
            if (username.equals("") || password.equals(""))
                github = GitHub.connectAnonymously();
            else
                github = GitHub.connectUsingPassword(username, password);
            Log.d("PlayerUpdater", "Getting repository " + repository_name + "...");
            try {
                repository = github.getRepository(repository_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("PlayerUpdater", "Got it");
        }
        return repository;
    }

    public interface GitHubTask {
        void contextDestroyed();
    }

    public interface GitHubCallbacks<GitHubResult> {
        void onCancelled(String reason);
        void onPostExecute(GitHubResult result);
    }
}

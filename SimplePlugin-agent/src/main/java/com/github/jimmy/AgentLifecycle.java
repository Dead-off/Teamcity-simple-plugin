package com.github.jimmy;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * @author Maxim.Zaytsev
 * Date: 4/24/18
 * @since 1.0
 */
public class AgentLifecycle extends AgentLifeCycleAdapter {

  @NotNull
  private final ArtifactsWatcher myArtifactsWatcher;

  public AgentLifecycle(@NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                        @NotNull final ArtifactsWatcher artifactsWatcher) {
    eventDispatcher.addListener(this);
    myArtifactsWatcher = artifactsWatcher;
  }

  @Override
  public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
    Loggers.AGENT.debug("invoked beforeBuildFinish callback");
    File providedArtifact = new File(build.getBuildTempDirectory(), "large_file_from_plugin.binary");
    Random random = new Random();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(providedArtifact);
      for (int i = 0; i < 500; i++) {
        byte[] b = new byte[1024 * 1024];
        random.nextBytes(b);
        fos.write(b);
      }
    } catch (IOException e) {
      Loggers.AGENT.warnAndDebugDetails("Unable to writing content to file " + providedArtifact, e);
      return;
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e1) {
          Loggers.AGENT.warnAndDebugDetails("Unable to close output stream", e1);
        }
      }
    }
    myArtifactsWatcher.addNewArtifactsPath(providedArtifact.getAbsolutePath() + "=>" + providedArtifact.getName());
  }
}

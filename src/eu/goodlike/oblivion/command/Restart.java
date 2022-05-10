package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Write;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.nio.file.Paths;

import static eu.goodlike.oblivion.SpellStackingCalculator.writeIntro;
import static eu.goodlike.oblivion.SpellStackingCalculator.writeSettings;

/**
 * Resets fucking everything, including the bloody configuration files!
 * <p/>
 * Use this command when you have made some changes to the settings or prepared files.
 * Equivalent to restarting the application.
 * Hence the name.
 */
public final class Restart extends BaseCommand {

  @Override
  protected void performTask() {
    Write.line("Application restarting.");
    Write.separator();

    try {
      manuallyRefreshTheConfigFiles();
    }
    catch (Exception e) {
      throw new IllegalStateException("Well, we tried to process the config files, didn't work.", e);
    }

    Global.initializeEverything();

    writeIntro();
    writeSettings();
  }

  private void manuallyRefreshTheConfigFiles() {
    File thisProject = Paths.get("").toFile();
    GradleConnector connector = GradleConnector.newConnector()
      .forProjectDirectory(thisProject);

    try (ProjectConnection connection = connector.connect()) {
      connection.newBuild().forTasks("processResources").run();
    }
  }

}

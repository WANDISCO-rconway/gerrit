package com.google.gerrit.gerritconsoleapi.cli.commands;

import com.google.common.base.Strings;
import com.google.gerrit.gerritconsoleapi.cli.processing.AllProjectsInProcessLoader;
import com.google.gerrit.gerritconsoleapi.cli.processing.CliCommandItemBase;
import com.google.gerrit.gerritconsoleapi.cli.processing.CmdLineParserFactory;
import com.google.gerrit.gerritconsoleapi.exceptions.LogAndExitException;

import com.google.gerrit.sshd.CommandMetaData;
import com.wandisco.gerrit.gitms.shared.config.lfs.LfsConfigFactory;
import com.wandisco.gerrit.gitms.shared.config.lfs.LfsProjectConfigSection;
import com.wandisco.gerrit.gitms.shared.config.lfs.LfsStorageBackend;
import com.wandisco.gerrit.gitms.shared.properties.GitMsApplicationProperties;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

import static com.google.gerrit.gerritconsoleapi.LfsRepositoryUtilities.*;


@CommandMetaData(name = "lfs-info", description = "Lfs content storage location of the backend belonging to the specified repository.")
public class LfsInformationCommand extends CliCommandItemBase {

  private LfsConfigFactory configFactory;
  private final GitMsApplicationProperties applicationProperties;

  @Option(name = "--repositoryname", aliases = "-r", usage = "The repository name to obtain LFS configuration information about.", metaVar = "lfstest01.git", required = true)
  private String repositoryName;

  // We now default git config location using the standard rules employed by the installer scripts which is environment $GIT_CONFIG, or user.home/.gitconfig
  // if you specify this arg it will overrule these.
  @Option(name = "--git-config", aliases = "-g", usage = "The location of the .gitconfig configuration file.", metaVar = "~/.gitconfig or /opt/wandisco/gitms/.gitconfig", required = false)
  private String gitConfigArg;

  public LfsInformationCommand() {
    super("lfs-info");

    try {
      applicationProperties = new GitMsApplicationProperties();
    } catch (IOException e) {
      debugStackTrace(e);
      throw new RuntimeException( new LogAndExitException("Unable to obtain LFS configuration information.", e));
    }

    try {
      configFactory = LfsConfigFactory.getInstance(applicationProperties.getGerritRoot());
      AllProjectsInProcessLoader allProjectsLoader = new AllProjectsInProcessLoader(gitConfigArg);

      configFactory.setAllProjectsLoaderCallback(allProjectsLoader);
    } catch (Exception e) {
      debugStackTrace(e);
      throw new RuntimeException( new LogAndExitException("Unable to obtain LFS configuration information.", e));
    }
  }

  @Override
  public void execute() throws LogAndExitException {

    // check the repo is valid -> and can be found in gerrit_repo_home.
    validateRepositoryIsReal(applicationProperties, repositoryName);

    // Now validate its an LFS type repo.
    try {
      if ( !configFactory.getLfsAllProjectsConfig().checkIfProjectIsLfs(repositoryName) )
      {
        // this project isn't recognised as LFS.
        logwarning(String.format("The repository given: {%s} is not recognised as a valid LFS repository.\n" +
            "Please check the repository name, or validate the repository status, in the UI", repositoryName));
        return;
      }
    } catch (Exception e) {
      throw new LogAndExitException("Unable to obtain information about whether this repository is LFS enabled. Details: ", e);
    }

    // it is a repo, get its LFS Project configuration.
    LfsProjectConfigSection reposLfsConfiguration = null;
    try {
      reposLfsConfiguration = configFactory.getLfsAllProjectsConfig().getSpecificProjectsLfsConfig(repositoryName);
    } catch (Exception e) {
      throw new LogAndExitException("Unable to obtain information repository LFS configuration information. Details: ", e);
    }

    // Turn into a set of name / value pairs, representing the LFS configuration for this project for ease of use later.
    Map<String, String> lfsconfiginfo = getConfigurationMapOfValues(reposLfsConfiguration);

    Path lfsStorageLocation;
    try {
      lfsStorageLocation = getLFSRepoStorageLocation( repositoryName,
          reposLfsConfiguration.getBackend(),
          configFactory.getGerritServerLfsConfig().getGerritRootDir(),
          configFactory.getGerritServerLfsConfig().getDefaultBackendDirectory());
    } catch (Exception e) {
      throw new LogAndExitException("Unable to obtain LFS backend storage location. Details: ", e);
    }

    // Now we have the backend get the information about it.
    /*
     * we dont really need to, for now we always know its FS, if we support S3, put this backin.
     */
    /**
     * LfsStorageBackend backendStorage = getBackendByNamespace(reposLfsConfiguration.getBackend(), configFactory);
     * lfsconfiginfo.put("type", backendStorage.type.toString() );
     */
    lfsconfiginfo.put("type", "FS" );
    lfsconfiginfo.put("directory", lfsStorageLocation.toFile().getPath());

    // Output each of the lfs configuration items to the caller as name value pairs.
    // TODO maybe allow config option to control this output to say json / xml or name/value pairs.
    for ( Map.Entry<String, String> lfsItem : lfsconfiginfo.entrySet()){
      System.out.println(String.format("%s=%s", lfsItem.getKey(), lfsItem.getValue()));
    }

  }

}
/**
 *
 * marcosnr
 * 30/03/2012
 */
package au.org.aurin.wif.repo.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.org.aurin.wif.exception.config.WifInvalidConfigException;
import au.org.aurin.wif.exception.validate.WifInvalidInputException;
import au.org.aurin.wif.io.CouchDBManager;
import au.org.aurin.wif.io.CouchMapper;
import au.org.aurin.wif.model.WifProject;
import au.org.aurin.wif.repo.WifProjectDao;
import au.org.aurin.wif.svc.WifKeys;

// 
/**
 * The Class CouchWifProjectDao.
 */
@Component("WifProjectDao")
public class CouchWifProjectDao implements WifProjectDao {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CouchWifProjectDao.class);

  /** The converter. */
  @Autowired
  private CouchMapper converter;

  /** The manager. */
  @Autowired
  private CouchDBManager manager;

  /** The repository. */
  private WifProjectRepository repository;

  /**
   * Inits the.
   */

  @PostConstruct
  public void init() {
    LOGGER.trace("Initializing version: " + WifKeys.WIF_KEY_VERSION);
    repository = new WifProjectRepository(manager.getDb());

  }

  /**
   * Cleanup.
   */

  @PreDestroy
  public void cleanup() {
    LOGGER.trace("successfully cleared! ");
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.repo.WifProjectDao#persistProject(au.org.aurin.wif.model
   * .WifProject)
   */
  public WifProject persistProject(WifProject project)
      throws WifInvalidInputException, WifInvalidConfigException {
    WifProject mapProject;
    project.setCreationDate(new Date(System.currentTimeMillis()));
    mapProject = converter.map(project);
//    String msg = WifKeys.STANDARD_ALREADY_EXISTS_MSG + project.getId()
//        + manager.getDb().getDbInfo().getDbName();

    String msg = WifKeys.STANDARD_ALREADY_EXISTS_MSG + project.getId()
            + manager.getDb().getDatabaseName();
    try {
      repository.add(mapProject);
    } catch (java.lang.IllegalArgumentException e) {
      LOGGER.error(WifKeys.STANDARD_ALREADY_EXISTS_MSG, project.getId(),
          manager.getDb().getDbInfo().getDbName());
      throw new WifInvalidInputException(msg, e);
    }
    return project;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.repo.WifProjectDao#addProject(au.org.aurin.wif.model.
   * WifProject)
   */
  public void addProject(WifProject project) {
    repository.add(project);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.repo.WifProjectDao#updateProject(au.org.aurin.wif.model
   * .WifProject)
   */
  public void updateProject(WifProject project) {
    LOGGER.debug("Updating {}, revision {}", project.getId(),
        project.getRevision());
    project.setModifiedDate(new Date(System.currentTimeMillis()));
    repository.update(project);
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.repo.WifProjectDao#findProjectById(java.lang.String)
   */
  public WifProject findProjectById(String id) {
    try {
      WifProject wifProject = repository.get(id);
      return wifProject;
    } catch (org.ektorp.DocumentNotFoundException e) {
      LOGGER.warn(WifKeys.STANDARD_ID_NOT_FOUND_MSG, id, manager.getDb()
          .getDbInfo().getDbName());
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.repo.WifProjectDao#deleteProject(au.org.aurin.wif.model
   * .WifProject)
   */
  public void deleteProject(WifProject project) {
    repository.remove(project);
  }

  // FIXME for now it's not needed, but we should implementIT, for some reason
  // it's not working
  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.repo.WifProjectDao#getAllProjects()
   */
  public List<WifProject> getAllProjects() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.repo.WifProjectDao#getAllProjects(java.lang.String)
   */
  public List<WifProject> getAllProjects(String roleOwner) {

    List<WifProject> outList = new ArrayList<WifProject>();
    if (roleOwner.equals(WifKeys.SHIB_ROLE_NAME)) {
      // can see all projects
      outList = repository.getAllProjects();
    } else {
      outList = repository.findByRoleOwner(roleOwner);
      // since all users can see the Perth Demonstration Project.
      for (WifProject wif : repository.findByName(WifKeys.TEST_PROJECT_NAME)) {
        outList.add(wif);
      }
    }

    return outList;

    // return repository.findByRoleOwner(roleOwner);
  }

  /**
   * The Class WifProjectRepository.
   */
  public class WifProjectRepository extends
      CouchDbRepositorySupport<WifProject> {

    /**
     * Instantiates a new wif project repository.
     * 
     * @param db
     *          the db
     */
    @Autowired
    public WifProjectRepository(CouchDbConnector db) {
      super(WifProject.class, db);
      initStandardDesignDocument();
    }

    /**
     * Find by role.
     * 
     * @param roleOwner
     *          the role owner
     * @return the list
     */
    @GenerateView
    public List<WifProject> findByRoleOwner(String roleOwner) {
      return queryView("by_roleOwner", roleOwner);
    }

    /**
     * Find by name.
     * 
     * @param name
     *          the name
     * @return the list
     */
    @GenerateView
    public List<WifProject> findByName(String name) {
      return queryView("by_name", name);
    }

    @View(name = "getAllProjects", map = "function(doc) { if(doc.docType=='WifProject') {emit(doc)} }")
    public List<WifProject> getAllProjects() {
      return queryView("getAllProjects");
    }
  }
}

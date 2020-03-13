package au.org.aurin.wif.restclient;

import static au.org.aurin.wif.io.RestAPIConstants.HEADER_USER_ID_KEY;
import static au.org.aurin.wif.io.RestUtil.removeTrailingSlash;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import au.org.aurin.wif.controller.OWIURLs;
import au.org.aurin.wif.io.ServiceException;
import au.org.aurin.wif.model.WifProject;
import au.org.aurin.wif.model.reports.ProjectReport;

/**
 * The Class ProjectServiceClientImpl.
 */
public class ProjectServiceClientImpl implements ProjectServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProjectServiceClientImpl.class);

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#setUrl(java.lang.String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.ProjectServiceClient#getVersion()
   */
  public Map<String, String> getVersion() {
    final ResponseEntity<Map> response = restTemplate.getForEntity(
        removeTrailingSlash(url) + "/version", Map.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + "/version.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getProjectRevision(java
   * .lang.String, java.lang.String)
   */
  public String getProjectRevision(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<String> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/revision", HttpMethod.GET, requestEntity, String.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/revision");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getProject(java.lang.String
   * , java.lang.String)
   */
  public HashMap<String, String> getStatus(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<HashMap> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/status", HttpMethod.GET, requestEntity, HashMap.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getProject(java.lang.String
   * , java.lang.String)
   */
  public WifProject getProject(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<WifProject> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id,
        HttpMethod.GET, requestEntity, WifProject.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id + ".");
    }
    return response.getBody();
  }

  public boolean downloadZip(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<Object> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/zipUAZ", HttpMethod.GET, requestEntity, Object.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/zipUAZ");
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#updateProject(java.lang
   * .String, java.lang.String, au.org.aurin.wif.model.WifProject)
   */
  public void updateProject(final String roleId, final String id,
      final WifProject wifProject) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(wifProject,
        headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id, HttpMethod.PUT, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#setupAllocationConfig(
   * java.lang.String, java.lang.String, au.org.aurin.wif.model.WifProject)
   */
  public void setupAllocationConfig(final String roleId, final String id,
      final WifProject wifProject) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(wifProject,
        headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id + "/allocation/setup", HttpMethod.PUT, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#deleteProject(java.lang
   * .String, java.lang.String)
   */
  public void deleteProject(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#updateUAZ(java.lang.String
   * , java.util.List)
   */
  public void finalizeUAZ(final String roleId, final String id,
      final List<String> factorsList) {
    final String urlquery = removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id + "/UAZ";
    LOGGER.debug("Using link: " + urlquery);
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<List<String>> requestEntity = new HttpEntity<List<String>>(
        factorsList, headers);
    final ResponseEntity<Object> response = restTemplate.exchange(urlquery,
        HttpMethod.PUT, requestEntity, Object.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException(
          "HTTP Response Status Code: "
              + response.getStatusCode()
              + " was thrown while accessing the ProjectServiceClientImpl.finalizeUAZ(roleId, projectId).");
    }

  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#createProject(java.lang
   * .String, au.org.aurin.wif.model.WifProject)
   */
  public String createProject(final String roleId, final WifProject project) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    LOGGER.debug("Using link: " + removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/");
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(project,
        headers);
    final ResponseEntity<WifProject> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/",
        HttpMethod.POST, requestEntity, WifProject.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException(
          "HTTP Response Status Code: "
              + response.getStatusCode()
              + " was thrown while accessing the ProjectServiceClientImpl.createProject(roleId, projectId).");
    }
    return response.getBody().getId().toString();

  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getUAZAttributes(java.
   * lang.String)
   */
  public List<String> getUAZAttributes(final String roleId, final String id) {
    // ResponseEntity<List> response = restTemplate.getForEntity(
    // removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
    // + "/unionAttributes", List.class);
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    LOGGER.debug("Using link: " + removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/");
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/unionAttributes", HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/unionAttributes.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.ProjectServiceClient#
   * getDistinctEntriesForUAZAttribute(java.lang.String, java.lang.String)
   */
  public List<String> getDistinctEntriesForUAZAttribute(final String roleId,
      final String id, final String attr) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    LOGGER.debug("Using link: " + removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/");
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/unionAttributes/" + attr + "/values", HttpMethod.GET,
        requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/unionAttribute/" + attr + "/values.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getAllProjects(java.lang
   * .String)
   */
  public List<WifProject> getAllProjects(final String role) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, role);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/",
        HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/");
    }
    return response.getBody();

  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getProjectConfiguration
   * (java.lang.String, java.lang.String)
   */
  public WifProject getProjectConfiguration(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<WifProject> response = restTemplate
        .exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/configuration", HttpMethod.GET, requestEntity, WifProject.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/configuration" + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#restoreProject(java.lang
   * .String, au.org.aurin.wif.model.WifProject)
   */
  public String restoreProject(final String roleId,
      final ProjectReport projectReport) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    LOGGER.debug("Using link: " + removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/restore");
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        projectReport, headers);
    final ResponseEntity<WifProject> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/restore",
        HttpMethod.POST, requestEntity, WifProject.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException(
          "HTTP Response Status Code: "
              + response.getStatusCode()
              + " was thrown while accessing the ProjectServiceClientImpl.restoreProject(roleId, projectId).");
    }
    return response.getBody().getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#deleteProject(java.lang
   * .String, java.lang.String, boolean)
   */
  public void deleteProject(final String roleId, final String id,
      final boolean deleteUAZ) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    String option = "";
    if (!deleteUAZ) {
      option = "/keepUAZ";
    }

    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id + option, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getProjectReport(java.
   * lang.String, java.lang.String)
   */
  public ProjectReport getProjectReport(final String roleId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<ProjectReport> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/report", HttpMethod.GET, requestEntity, ProjectReport.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#getUploadStatus(java.lang
   * .String, java.lang.String)
   */
  public HashMap<String, String> getUploadStatus(final String roleId,
      final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<HashMap> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
            + "/upload/status", HttpMethod.GET, requestEntity, HashMap.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + id
          + "/upload/status");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#uploadUAZ(java.lang.String
   * , java.lang.String)
   */
  public void uploadUAZ(final String roleId, final String id) {
    final String urlquery = removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + id + "/upload";
    LOGGER.debug("Using link: " + urlquery);
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);

    final ResponseEntity<Object> response = restTemplate.exchange(urlquery,
        HttpMethod.POST, requestEntity, Object.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode());
    }

  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.ProjectServiceClient#uploadZip(java.io.File)
   */
  public String uploadZip(final File input) {

    final MultiValueMap<String, HttpEntity<FileSystemResource>> parts = new LinkedMultiValueMap<String, HttpEntity<FileSystemResource>>();
    final HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.add("Content-type", MediaType.MULTIPART_FORM_DATA_VALUE);
    final FileSystemResource r = new FileSystemResource(input);
    final HttpEntity<FileSystemResource> sample_file = new HttpEntity<FileSystemResource>(
        r, fileHeaders);
    parts.add("file", sample_file);
    final HttpHeaders reqheaders = new HttpHeaders();
    final HttpEntity<MultiValueMap<String, HttpEntity<FileSystemResource>>> ereq = new HttpEntity<MultiValueMap<String, HttpEntity<FileSystemResource>>>(
        parts, reqheaders);
    final ResponseEntity<String> response = restTemplate.exchange(
        removeTrailingSlash(url) + "/saveZip", HttpMethod.POST, ereq,
        String.class);
    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against /saveZip for "
          + input.getName());
    }
    return response.getBody();

  }
}

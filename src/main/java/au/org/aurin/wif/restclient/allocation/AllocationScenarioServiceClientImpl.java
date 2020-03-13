package au.org.aurin.wif.restclient.allocation;

import static au.org.aurin.wif.io.RestAPIConstants.HEADER_USER_ID_KEY;
import static au.org.aurin.wif.io.RestUtil.removeTrailingSlash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindException;
import org.springframework.web.client.RestTemplate;

import au.org.aurin.wif.controller.OWIURLs;
import au.org.aurin.wif.exception.config.WifInvalidConfigException;
import au.org.aurin.wif.exception.validate.WifInvalidInputException;
import au.org.aurin.wif.impl.suitability.WMSOutcome;
import au.org.aurin.wif.io.ServiceException;
import au.org.aurin.wif.model.allocation.AllocationScenario;
import au.org.aurin.wif.model.reports.allocation.AllocationAnalysisReport;

/**
 * The Class AllocationScenarioServiceClientImpl.
 */
public class AllocationScenarioServiceClientImpl implements
    AllocationScenarioServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(AllocationScenarioServiceClientImpl.class);

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationScenarioServiceClient#setUrl(java
   * .lang.String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationScenarioServiceClient#
   * getAllocationScenariosForProject(java.lang.String, java.lang.String)
   */
  public List<AllocationScenario> getAllocationScenariosForProject(
      final String roleId, final String projectId)
      throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationScenarios/", HttpMethod.GET, requestEntity,
        List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/allocationScenarios/.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationScenarioServiceClient#
   * getAllocationScenario(java.lang.String, java.lang.String, java.lang.String)
   */
  public AllocationScenario getAllocationScenario(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<AllocationScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationScenarios/" + id, HttpMethod.GET, requestEntity,
        AllocationScenario.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/allocationScenarios/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationScenarioServiceClient#
   * createAllocationScenario(java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.allocation.AllocationScenario)
   */
  public String createAllocationScenario(final String roleId,
      final String projectId, final AllocationScenario allocationScenario)
      throws WifInvalidInputException, BindException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        allocationScenario, headers);
    final ResponseEntity<AllocationScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationScenarios/", HttpMethod.POST, requestEntity,
        AllocationScenario.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating an AllocationScenario.");
    }
    final AllocationScenario persistedAllocationScenario = response.getBody();
    return persistedAllocationScenario.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationScenarioServiceClient#
   * updateAllocationScenario(java.lang.String, java.lang.String,
   * java.lang.String, au.org.aurin.wif.model.allocation.AllocationScenario)
   */
  public void updateAllocationScenario(final String roleId,
      final String projectId, final String id,
      final AllocationScenario allocationScenario)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        allocationScenario, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/allocationScenarios/" + id, HttpMethod.PUT,
        requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationScenarioServiceClient#
   * deleteAllocationScenario(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void deleteAllocationScenario(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/" + projectId + "/allocationScenarios/"
        + id;
    restTemplate.exchange(requestURL, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationScenarioServiceClient#getWMSOutcomeAsync
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void getOutcomeAsync(final String roleId, final String projectId,
      final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);

    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);

    final Map<String, String> urlparams = new HashMap<String, String>();
    urlparams.put("projectId", projectId);
    urlparams.put("id", id);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI
        + "/{projectId}/allocationScenarios/{id}/asyncOutcome";
    LOGGER.debug("getOutcomeAsync " + requestURL);
    final ResponseEntity<Object> response = restTemplate.exchange(requestURL,
        HttpMethod.PUT, requestEntity, Object.class, urlparams);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while accessing the getOutcomeAsync");
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationScenarioServiceClient#getStatus(java
   * .lang.String, java.lang.String, java.lang.String)
   */
  public HashMap<String, String> getStatus(final String roleId,
      final String projectId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final Map<String, String> urlparams = new HashMap<String, String>();
    urlparams.put("projectId", projectId);
    urlparams.put("id", id);

    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI
        + "/{projectId}/allocationScenarios/{id}/status";

    final ResponseEntity<HashMap> response = restTemplate.exchange(requestURL,
        HttpMethod.GET, requestEntity, HashMap.class, urlparams);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against " + requestURL);
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.allocation.AllocationScenarioServiceClient#
   * getWMS(java.lang.String, java.lang.String, java.lang.String)
   */
  public WMSOutcome getWMS(final String roleId, final String projectId,
      final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final Map<String, String> urlparams = new HashMap<String, String>();
    urlparams.put("projectId", projectId);
    urlparams.put("id", id);

    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI
        + "/{projectId}/allocationScenarios/{id}/wmsinfo";

    final ResponseEntity<WMSOutcome> response = restTemplate.exchange(
        requestURL, HttpMethod.GET, requestEntity, WMSOutcome.class, urlparams);

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
   * au.org.aurin.wif.restclient.allocation.AllocationScenarioServiceClient#
   * getAllocationScenarioReport(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public AllocationAnalysisReport getAllocationScenarioReport(
      final String roleId, final String projectId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<AllocationAnalysisReport> response = restTemplate
        .exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
            + projectId + "/allocationScenarios/" + id + "/report",
            HttpMethod.GET, requestEntity, AllocationAnalysisReport.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/allocationScenarios/" + id + "/report");
    }
    return response.getBody();
  }
}

package au.org.aurin.wif.restclient.suitability;

import static au.org.aurin.wif.io.RestAPIConstants.HEADER_USER_ID_KEY;
import static au.org.aurin.wif.io.RestUtil.removeTrailingSlash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import au.org.aurin.wif.model.reports.suitability.SuitabilityAnalysisReport;
import au.org.aurin.wif.model.suitability.SuitabilityScenario;

/**
 * The Class SuitabilityScenarioServiceClientImpl.
 */
public class SuitabilityScenarioServiceClientImpl implements
    SuitabilityScenarioServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#setUrl(java
   * .lang.String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#
   * getSuitabilityScenariosForProject(java.lang.String, java.lang.String)
   */
  public List<SuitabilityScenario> getSuitabilityScenariosForProject(
      final String roleId, final String projectId)
      throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityScenarios/", HttpMethod.GET, requestEntity,
        List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityScenarios/.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#
   * getSuitabilityScenario(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public SuitabilityScenario getSuitabilityScenario(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<SuitabilityScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityScenarios/" + id, HttpMethod.GET, requestEntity,
        SuitabilityScenario.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityScenarios/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#
   * createSuitabilityScenario(java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.suitability.SuitabilityScenario)
   */
  public String createSuitabilityScenario(final String roleId,
      final String projectId, final SuitabilityScenario suitabilityScenario)
      throws WifInvalidInputException, BindException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        suitabilityScenario, headers);
    final ResponseEntity<SuitabilityScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityScenarios/", HttpMethod.POST, requestEntity,
        SuitabilityScenario.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating an SuitabilityScenario.");
    }
    final SuitabilityScenario persistedSuitabilityScenario = response.getBody();
    return persistedSuitabilityScenario.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#
   * updateSuitabilityScenario(java.lang.String, java.lang.String,
   * java.lang.String, au.org.aurin.wif.model.suitability.SuitabilityScenario)
   */
  public void updateSuitabilityScenario(final String roleId,
      final String projectId, final String id,
      final SuitabilityScenario suitabilityScenario)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        suitabilityScenario, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/suitabilityScenarios/" + id, HttpMethod.PUT,
        requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#
   * deleteSuitabilityScenario(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void deleteSuitabilityScenario(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/" + projectId + "/suitabilityScenarios/"
        + id;
    restTemplate.exchange(requestURL, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#getWMSOutcomeAsync
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void getWMSOutcomeAsync(final String roleId, final String projectId,
      final String id, final String areaAnalyzed, final String crsArea)
      throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);

    final Map<String, String> reqparams = new HashMap<String, String>();
    reqparams.put("areaAnalyzed", areaAnalyzed);
    reqparams.put("crsArea", crsArea);

    final HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(
        reqparams, headers);
    final Map<String, String> urlparams = new HashMap<String, String>();
    urlparams.put("projectId", projectId);
    urlparams.put("id", id);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI
        + "/{projectId}/suitabilityScenarios/{id}/async/wms";

    final ResponseEntity<Object> response = restTemplate.exchange(requestURL,
        HttpMethod.POST, entity, Object.class, urlparams);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while accessing the getWMSOutcomeAsync");
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#getWMSOutcome
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public WMSOutcome getWMSOutcome(final String roleId, final String projectId,
      final String id, final String areaAnalyzed, final String crsArea) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set(HEADER_USER_ID_KEY, roleId);
    final Map<String, String> reqparams = new HashMap<String, String>();
    reqparams.put("areaAnalyzed", areaAnalyzed);
    reqparams.put("crsArea", crsArea);

    final HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(
        reqparams, headers);
    final Map<String, String> urlparams = new HashMap<String, String>();
    urlparams.put("projectId", projectId);
    urlparams.put("id", id);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI
        + "/{projectId}/suitabilityScenarios/{id}/wms";

    final ResponseEntity<WMSOutcome> response = restTemplate.exchange(
        requestURL, HttpMethod.POST, entity, WMSOutcome.class, urlparams);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/suitabilityScenarios/" + id + "/wms");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityScenarioServiceClient#getStatus(
   * java.lang.String, java.lang.String, java.lang.String)
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
        + "/{projectId}/suitabilityScenarios/{id}/status";

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
   * au.org.aurin.wif.restclient.ProjectServiceClient#getWMS(java.lang.String,
   * java.lang.String)
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
        + "/{projectId}/suitabilityScenarios/{id}/wmsinfo";

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
   * au.org.aurin.wif.restclient.suitability.SuitabilityScenarioServiceClient
   * #getSuitabilityScenarioReport(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public SuitabilityAnalysisReport getSuitabilityScenarioReport(
      final String roleId, final String projectId, final String id) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<SuitabilityAnalysisReport> response = restTemplate
        .exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
            + projectId + "/suitabilityScenarios/" + id + "/report",
            HttpMethod.GET, requestEntity, SuitabilityAnalysisReport.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityScenarios/" + id + "/report");
    }
    return response.getBody();
  }
}

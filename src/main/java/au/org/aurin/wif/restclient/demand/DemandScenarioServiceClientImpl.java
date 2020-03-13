package au.org.aurin.wif.restclient.demand;

import static au.org.aurin.wif.io.RestAPIConstants.HEADER_USER_ID_KEY;
import static au.org.aurin.wif.io.RestUtil.removeTrailingSlash;

import java.util.List;

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
import au.org.aurin.wif.io.ServiceException;
import au.org.aurin.wif.model.demand.AreaRequirement;
import au.org.aurin.wif.model.demand.DemandScenario;
import au.org.aurin.wif.model.reports.demand.DemandAnalysisReport;

/**
 * The Class DemandScenarioServiceClientImpl.
 */
public class DemandScenarioServiceClientImpl implements
    DemandScenarioServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandScenarioServiceClient#setUrl(java.lang
   * .String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.DemandScenarioServiceClient#
   * getDemandScenariosForProject(java.lang.String, java.lang.String)
   */
  public List<DemandScenario> getDemandScenariosForProject(final String roleId,
      final String projectId) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demandScenarios/", HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/demandScenarios/.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandScenarioServiceClient#getDemandScenario
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public DemandScenario getDemandScenario(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<DemandScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demandScenarios/" + id, HttpMethod.GET, requestEntity,
        DemandScenario.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/demandScenarios/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandScenarioServiceClient#createDemandScenario
   * (java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.demand.DemandScenario)
   */
  public String createDemandScenario(final String roleId,
      final String projectId, final DemandScenario demandScenario)
      throws WifInvalidInputException, BindException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        demandScenario, headers);
    final ResponseEntity<DemandScenario> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demandScenarios/", HttpMethod.POST, requestEntity,
        DemandScenario.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating an DemandScenario.");
    }
    final DemandScenario persistedDemandScenario = response.getBody();
    return persistedDemandScenario.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandScenarioServiceClient#updateDemandScenario
   * (java.lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.demand.DemandScenario)
   */
  public void updateDemandScenario(final String roleId, final String projectId,
      final String id, final DemandScenario demandScenario)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        demandScenario, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/demandScenarios/" + id, HttpMethod.PUT,
        requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandScenarioServiceClient#deleteDemandScenario
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteDemandScenario(final String roleId, final String projectId,
      final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final String requestURL = removeTrailingSlash(url)
        + OWIURLs.PROJECT_SVC_URI + "/" + projectId + "/demandScenarios/" + id;
    restTemplate.exchange(requestURL, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.demand.DemandScenarioServiceClient#
   * getDemandScenarioOutcome(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public List<AreaRequirement> getDemandScenarioOutcome(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demandScenarios/" + id + "/outcome", HttpMethod.GET,
        requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/demandScenarios/" + id + "/outcome");
    }
    return response.getBody();
  }

  public DemandAnalysisReport getDemandScenarioReport(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<DemandAnalysisReport> response = restTemplate
        .exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
            + projectId + "/demandScenarios/" + id + "/report", HttpMethod.GET,
            requestEntity, DemandAnalysisReport.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/demandScenarios/" + id + "/report");
    }
    return response.getBody();
  }
}

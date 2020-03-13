package au.org.aurin.wif.restclient.suitability;

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
import au.org.aurin.wif.model.suitability.SuitabilityLU;

/**
 * The Class SuitabilityLUServiceClientImpl.
 */
public class SuitabilityLUServiceClientImpl implements
    SuitabilityLUServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#setUrl(java.lang
   * .String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.SuitabilityLUServiceClient#
   * getSuitabilityLUsForProject(java.lang.String, java.lang.String)
   */
  public List<SuitabilityLU> getSuitabilityLUsForProject(final String roleId,
      final String projectId) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityLUs", HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityLUs.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#getSuitabilityLU
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public SuitabilityLU getSuitabilityLU(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<SuitabilityLU> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityLUs/" + id, HttpMethod.GET, requestEntity,
        SuitabilityLU.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityLUs/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#createSuitabilityLU
   * (java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.suitability.SuitabilityLU)
   */
  public String createSuitabilityLU(final String roleId,
      final String projectId, final SuitabilityLU suitabilityLU)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        suitabilityLU, headers);
    final ResponseEntity<SuitabilityLU> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityLUs/", HttpMethod.POST, requestEntity,
        SuitabilityLU.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating a SuitabilityLU.");
    }
    final SuitabilityLU persistedSuitabilityLU = response.getBody();
    return persistedSuitabilityLU.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#updateSuitabilityLU
   * (java.lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.suitability.SuitabilityLU)
   */
  public void updateSuitabilityLU(final String roleId, final String projectId,
      final String id, final SuitabilityLU suitabilityLU)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        suitabilityLU, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/suitabilityLUs/" + id, HttpMethod.PUT,
        requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#deleteSuitabilityLU
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteSuitabilityLU(final String roleId, final String projectId,
      final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/suitabilityLUs/" + id, HttpMethod.DELETE,
        requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#addAssociatedLU(
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public void addAssociatedLU(final String roleId, final String projectId,
      final String suitabilityLUId, final String id)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/suitabilityLUs/" + suitabilityLUId
        + "/associatedLUs/" + id, HttpMethod.PUT, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#deleteAssociatedLU
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteAssociatedLU(final String roleId, final String projectId,
      final String suitabilityLUId, final String id)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/suitabilityLUs/" + suitabilityLUId
        + "/associatedLUs/" + id, HttpMethod.DELETE, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.SuitabilityLUServiceClient#getAssociatedLUs
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public List<String> getAssociatedLUs(final String roleId,
      final String projectId, final String suitabilityLUId)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/suitabilityLUs/" + suitabilityLUId + "/associatedLUs",
        HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + roleId
          + "/" + projectId + "/suitabilityLUs/" + suitabilityLUId
          + "/associatedLUs.");
    }
    return response.getBody();
  }

}

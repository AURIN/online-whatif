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
import org.springframework.web.client.RestTemplate;

import au.org.aurin.wif.controller.OWIURLs;
import au.org.aurin.wif.exception.config.WifInvalidConfigException;
import au.org.aurin.wif.exception.validate.WifInvalidInputException;
import au.org.aurin.wif.io.ServiceException;
import au.org.aurin.wif.model.suitability.FactorType;

/**
 * The Class FactorTypeServiceClientImpl.
 */
public class FactorTypeServiceClientImpl implements FactorTypeServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /**
   * Sets the url.
   * 
   * @param url
   *          the new url
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.FactorTypeServiceClient#
   * getFactorTypesForProjectAndFactor(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public List<FactorType> getFactorTypesForProjectAndFactor(
      final String roleId, final String projectId, final String factorId)
      throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/factors/" + factorId + "/factortypes", HttpMethod.GET,
        requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/factors/" + factorId + "/factortypes.");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.FactorTypeServiceClient#getFactorType(java.
   * lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public FactorType getFactorType(final String roleId, final String projectId,
      final String factorId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<FactorType> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/factors/" + factorId + "/factortypes/" + id, HttpMethod.GET,
        requestEntity, FactorType.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/factors/" + factorId + "/factortypes/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.FactorTypeServiceClient#createFactorType(java
   * .lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.suitability.FactorType)
   */
  public String createFactorType(final String roleId, final String projectId,
      final String factorId, final FactorType factorType) {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(factorType,
        headers);
    final ResponseEntity<FactorType> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/factors/" + factorId + "/factortypes/", HttpMethod.POST,
        requestEntity, FactorType.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating a FactorType.");
    }
    final FactorType persistedFactorType = response.getBody();
    return persistedFactorType.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.FactorTypeServiceClient#updateFactorType(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.suitability.FactorType)
   */
  public void updateFactorType(final String roleId, final String projectId,
      final String factorId, final String id, final FactorType factorType)
      throws WifInvalidInputException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(factorType,
        headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/factors/" + factorId + "/factortypes/" + id,
        HttpMethod.PUT, requestEntity, void.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.FactorTypeServiceClient#deleteFactorType(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteFactorType(final String roleId, final String projectId,
      final String factorId, final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/factors/" + factorId + "/factortypes" + id,
        HttpMethod.DELETE, requestEntity, void.class);

  }

}

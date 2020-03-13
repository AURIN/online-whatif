package au.org.aurin.wif.restclient.demand;

import static au.org.aurin.wif.io.RestAPIConstants.HEADER_USER_ID_KEY;
import static au.org.aurin.wif.io.RestUtil.removeTrailingSlash;

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
import au.org.aurin.wif.model.demand.DemandConfig;

/**
 * The Class DemandConfigServiceClientImpl.
 */
public class DemandConfigServiceClientImpl implements DemandConfigServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandConfigServiceClient#setUrl(java.lang.
   * String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandConfigServiceClient#createDemandConfig
   * (java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.allocation.DemandConfig)
   */
  public String createDemandConfig(final String roleId, final String projectId,
      final DemandConfig demandConfig) throws WifInvalidInputException,
      BindException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        demandConfig, headers);
    final ResponseEntity<DemandConfig> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demand/setup/", HttpMethod.POST, requestEntity,
        DemandConfig.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating an DemandConfig.");
    }
    final DemandConfig persistedDemandConfig = response.getBody();
    return persistedDemandConfig.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandConfigServiceClient#getDemandConfig(java
   * .lang.String, java.lang.String, java.lang.String)
   */
  public DemandConfig getDemandConfig(final String roleId,
      final String projectId) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<DemandConfig> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/demand/setup/", HttpMethod.GET, requestEntity,
        DemandConfig.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/demand/setup/" + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandConfigServiceClient#updateDemandConfig
   * (java.lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.allocation.DemandConfig)
   */
  public void updateDemandConfig(final String roleId, final String projectId,
      final DemandConfig demandConfig) throws WifInvalidInputException,
      BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        demandConfig, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/demand/setup/", HttpMethod.PUT, requestEntity,
        Object.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.DemandConfigServiceClient#deleteDemandConfig
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteDemandConfig(final String roleId, final String projectId)
      throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/demand/setup/", HttpMethod.DELETE, requestEntity,
        Object.class);
  }

}

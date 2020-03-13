package au.org.aurin.wif.restclient;

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
import au.org.aurin.wif.model.allocation.AllocationLU;

/**
 * The Class AllocationLUServiceClientImpl.
 */
public class AllocationLUServiceClientImpl implements AllocationLUServiceClient {

  /** The url. */
  private String url;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationLUServiceClient#setUrl(java.lang.
   * String)
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see au.org.aurin.wif.restclient.AllocationLUServiceClient#
   * getAllocationLUsForProject(java.lang.String, java.lang.String)
   */
  public List<AllocationLU> getAllocationLUsForProject(final String roleId,
      final String projectId) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<List> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationLUs/", HttpMethod.GET, requestEntity, List.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/allocationLUs/.");
    }
    return response.getBody();

  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationLUServiceClient#getAllocationLU(java
   * .lang.String, java.lang.String, java.lang.String)
   */
  public AllocationLU getAllocationLU(final String roleId,
      final String projectId, final String id) throws WifInvalidInputException,
      WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    final ResponseEntity<AllocationLU> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationLUs/" + id, HttpMethod.GET, requestEntity,
        AllocationLU.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while running a request against "
          + removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/"
          + projectId + "/allocationLUs/" + id + ".");
    }
    return response.getBody();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationLUServiceClient#createAllocationLU
   * (java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.allocation.AllocationLU)
   */
  public String createAllocationLU(final String roleId, final String projectId,
      final AllocationLU allocationLU) throws WifInvalidInputException,
      BindException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        allocationLU, headers);
    final ResponseEntity<AllocationLU> response = restTemplate.exchange(
        removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI + "/" + projectId
            + "/allocationLUs/", HttpMethod.POST, requestEntity,
        AllocationLU.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ServiceException("HTTP Response Status Code: "
          + response.getStatusCode()
          + " was thrown while creating an AllocationLU.");
    }
    final AllocationLU persistedAllocationLU = response.getBody();
    return persistedAllocationLU.getId().toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationLUServiceClient#updateAllocationLU
   * (java.lang.String, java.lang.String, java.lang.String,
   * au.org.aurin.wif.model.allocation.AllocationLU)
   */
  public void updateAllocationLU(final String roleId, final String projectId,
      final String id, final AllocationLU allocationLU)
      throws WifInvalidInputException, BindException, WifInvalidConfigException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(
        allocationLU, headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/allocationLUs/" + id, HttpMethod.PUT,
        requestEntity, Object.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * au.org.aurin.wif.restclient.AllocationLUServiceClient#deleteAllocationLU
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteAllocationLU(final String roleId, final String projectId,
      final String id) throws WifInvalidInputException {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add(HEADER_USER_ID_KEY, roleId);
    final HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
    restTemplate.exchange(removeTrailingSlash(url) + OWIURLs.PROJECT_SVC_URI
        + "/" + projectId + "/allocationLUs/" + id, HttpMethod.DELETE,
        requestEntity, Object.class);
  }

}

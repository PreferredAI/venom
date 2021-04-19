package ai.preferred.venom.examples.propertyListingCrawler.master;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.response.VResponse;
import ai.preferred.venom.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
THE FOLLOWING EXAMPLE MODIFIED FROM https://github
.com/PreferredAI/venom-examples/blob/master/src/main/java/ai/preferred
/crawler/iproperty/master
 */
public class ListingValidator implements Validator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListingValidator.class);

  /**
   * Use this to positively validate your page.
   * <p>
   * For example, if you are crawling store ABC, you would find.
   * </p>
   *
   * @param request  The request used to fetch.
   * @param response The response fetched using request.
   * @return status of the validation
   */
  @Override
  public Validator.Status isValid(Request request, Response response) {
    final VResponse vResponse = new VResponse(response);

    if (vResponse.getHtml().contains("Property for rent in Singapore")) {
      return Status.VALID;
    }

    LOGGER.info("Invalid content");
    return Status.INVALID_CONTENT;
  }
}

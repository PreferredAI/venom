package ai.preferred.venom.validator;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidatorContextTest {

  private final Request request = new VRequest("https://venom.preferred.ai");
  private final Response response = new BaseResponse(200, null, null, null, null, null);

  private final Validator alwaysInvalid = (request, response) -> Validator.Status.INVALID_CONTENT;

  @Test
  public void testValidPassThrough() {
    Assertions.assertEquals(
        Validator.Status.VALID,
        new ValidatorContext(Validator.ALWAYS_VALID).isValid(request, response)
    );
  }

  @Test
  public void testInvalidPassThrough() {
    Assertions.assertEquals(
        Validator.Status.INVALID_CONTENT,
        new ValidatorContext(alwaysInvalid).isValid(request, response)
    );
  }

  @Test
  public void testGetValidator() {
    final ValidatorContext validatorContext = new ValidatorContext(alwaysInvalid);
    Assertions.assertEquals(alwaysInvalid, validatorContext.getValidator());
  }

  @Test
  public void testGetAndSetValidator() {
    final ValidatorContext validatorContext = new ValidatorContext(alwaysInvalid);
    // Check get
    Assertions.assertEquals(alwaysInvalid, validatorContext.getValidator());

    // Set
    validatorContext.setValidator(Validator.ALWAYS_VALID);

    // Check if set worked
    Assertions.assertEquals(Validator.ALWAYS_VALID, validatorContext.getValidator());

    // Check if validation changed
    Assertions.assertEquals(Validator.Status.VALID, validatorContext.isValid(request, response));
  }

}

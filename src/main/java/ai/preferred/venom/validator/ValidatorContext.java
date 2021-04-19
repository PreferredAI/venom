/**
 * Strategy Pattern implementation
 *  Allows user to dynamically change validator being used at runtime
 */
package ai.preferred.venom.validator;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;

/**
 * Context class that controls which validator is used.
 */
public class ValidatorContext {
    private Validator validator;

    /**
     *
     * @param validator
     *  validator used in form of new validator
     *      e.g.:
     *          ValidatorContext valCon = ValidatorContext(new PipelineValidator());
     */
    public ValidatorContext(Validator validator) {
        this.validator = validator;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    public Validator.Status executeValidator(Request request, Response response) {
        return validator.isValid(request, response);
    }
}

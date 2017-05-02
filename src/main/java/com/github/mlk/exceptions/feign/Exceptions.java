package com.github.mlk.exceptions.feign;

import static com.github.mlk.exceptions.Exceptions.ErrorResponse.Builder.anError;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.github.mlk.exceptions.Exceptions.ErrorResponse;
import feign.FeignException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class Exceptions {

  private static final Logger log = LoggerFactory.getLogger(Exceptions.class);

  private static final String SERVER_ERROR = "SERVER_ERROR";

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(FeignException.class)
  @ResponseBody
  public ErrorResponse handleHttpClientErrorException(HttpServletRequest httpServletRequest,
      FeignException e) {
    if (e.status() >= 400 && e.status() < 500) {
      log.error(
          format("Downstream call failed with status: %s and response: %s", e.status(),
              e.getMessage()), e);

      return anError()
          .withStatus(INTERNAL_SERVER_ERROR.value())
          .withUrl(httpServletRequest.getRequestURL().toString())
          .withMessage(SERVER_ERROR)
          .withDescription("Downstream call failed")
          .build();
    } else {
      log.error("Unexpected error handled", e);

      return anError()
          .withStatus(INTERNAL_SERVER_ERROR.value())
          .withUrl(httpServletRequest.getRequestURL().toString())
          .withMessage(SERVER_ERROR)
          .withDescription("Sorry, something failed.")
          .build();
    }
  }
}

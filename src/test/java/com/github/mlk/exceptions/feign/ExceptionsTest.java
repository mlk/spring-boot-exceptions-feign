package com.github.mlk.exceptions.feign;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import feign.FeignException;
import feign.Response;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionsTest {

  @Mock
  private Operation operation;

  private MockMvc mockMvc;

  private final String jsonContent = "{\"value\": \"data\"}";

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(
        new ExceptionsTestController(operation))
        .setControllerAdvice(new com.github.mlk.exceptions.feign.Exceptions())
        .build();
  }

  @Test
  public void throwsInternalServerErrorForUnhandled400FeignException() throws Exception {
    doThrow(FeignException.errorStatus("", Response.builder().headers(Collections.emptyMap()).status(400).build())).when(operation).action();

    mockMvc.perform(post("/test")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("status", is(500)))
        .andExpect(jsonPath("url", is("http://localhost/test")))
        .andExpect(jsonPath("message", is("SERVER_ERROR")))
        .andExpect(jsonPath("description", is("Downstream call failed")));
  }

  @Test
  public void throwsInternalServerErrorForUnhandled500FeignException() throws Exception {
    doThrow(FeignException.errorStatus("", Response.builder().headers(Collections.emptyMap()).status(500).build())).when(operation).action();

    mockMvc.perform(post("/test")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("status", is(500)))
        .andExpect(jsonPath("url", is("http://localhost/test")))
        .andExpect(jsonPath("message", is("SERVER_ERROR")))
        .andExpect(jsonPath("description", is("Sorry, something failed.")));
  }


  @RestController
  @RequestMapping("test")
  @Profile("never-load-me")
  public static class ExceptionsTestController {

    private final Operation operation;

    public ExceptionsTestController(Operation operation) {
      this.operation = operation;
    }

    @PostMapping
    public void post(@RequestBody Data data) {
      operation.action();
    }

    @GetMapping(params = "param")
    public Data get(String param) {
      return new Data();
    }
  }

  public interface Operation {

    Data action();
  }

  static class Data {

  }
}

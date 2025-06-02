package com.vbalan.rate_limiter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.vbalan.rate_limiter.exception.GlobalExceptionHandler;
import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.service.AuthenticationService;
import com.vbalan.rate_limiter.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

  @Mock private AuthenticationService authenticationService;

  @Mock private RateLimitService rateLimitService;

  @InjectMocks private ApiController apiController;

  private MockMvc mockMvc;

  private static final String VALID_CLIENT_ID_ONE = "client-1";
  private static final String VALID_CLIENT_ID_TWO = "client-2";
  private static final String INVALID_CLIENT_ID = "invalid-client";
  private static final String VALID_AUTH_HEADER_CLIENT_ONE = "Bearer " + VALID_CLIENT_ID_ONE;
  private static final String VALID_AUTH_HEADER_CLIENT_TWO = "Bearer " + VALID_CLIENT_ID_TWO;
  private static final String INVALID_AUTH_HEADER = "Bearer " + INVALID_CLIENT_ID;
  private static final String MALFORMED_AUTH_HEADER = "InvalidHeader";

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(apiController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void testFooEndpoint_ValidClientAndAllowedRequest_ReturnsSuccess() throws Exception {
    ClientConfiguration config = new ClientConfiguration(10, 5);
    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE)).thenReturn(config);
    when(rateLimitService.allowRequestForFoo(VALID_CLIENT_ID_ONE, config)).thenReturn(true);

    mockMvc
        .perform(
            get("/foo")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true));

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_ONE);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_ONE);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_ONE);
    verify(rateLimitService).allowRequestForFoo(VALID_CLIENT_ID_ONE, config);
  }

  @Test
  void testFooEndpoint_InvalidClient_ReturnsUnauthorized() throws Exception {
    when(authenticationService.extractClientId(INVALID_AUTH_HEADER)).thenReturn(INVALID_CLIENT_ID);
    when(authenticationService.isValidClient(INVALID_CLIENT_ID)).thenReturn(false);

    mockMvc
        .perform(
            get("/foo")
                .header("Authorization", INVALID_AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verify(authenticationService).extractClientId(INVALID_AUTH_HEADER);
    verify(authenticationService).isValidClient(INVALID_CLIENT_ID);
  }

  @Test
  void testFooEndpoint_RateLimitExceeded_ThrowsException() throws Exception {
    ClientConfiguration config = new ClientConfiguration(10, 5);
    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE)).thenReturn(config);
    when(rateLimitService.allowRequestForFoo(VALID_CLIENT_ID_ONE, config)).thenReturn(false);

    mockMvc
        .perform(
            get("/foo")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Rate limit exceeded"));

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_ONE);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_ONE);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_ONE);
    verify(rateLimitService).allowRequestForFoo(VALID_CLIENT_ID_ONE, config);
  }

  @Test
  void testFooEndpoint_MissingAuthorizationHeader_ReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/foo").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("No Authorization header provided"));

    verify(authenticationService, never()).extractClientId(any());
    verify(authenticationService, never()).isValidClient(any());
  }

  @Test
  void testFooEndpoint_MalformedAuthorizationHeader_ReturnsUnauthorized() throws Exception {
    when(authenticationService.extractClientId(MALFORMED_AUTH_HEADER)).thenReturn("");
    when(authenticationService.isValidClient("")).thenReturn(false);

    mockMvc
        .perform(
            get("/foo")
                .header("Authorization", MALFORMED_AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verify(authenticationService).extractClientId(MALFORMED_AUTH_HEADER);
    verify(authenticationService).isValidClient("");
  }

  @Test
  void testBarEndpoint_ValidClientAndAllowedRequest_ReturnsSuccess() throws Exception {
    ClientConfiguration config = new ClientConfiguration(15, 8);
    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE)).thenReturn(config);
    when(rateLimitService.allowRequestForBar(VALID_CLIENT_ID_ONE, config)).thenReturn(true);

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true));

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_ONE);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_ONE);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_ONE);
    verify(rateLimitService).allowRequestForBar(VALID_CLIENT_ID_ONE, config);
  }

  @Test
  void testBarEndpoint_InvalidClient_ReturnsUnauthorized() throws Exception {
    when(authenticationService.extractClientId(INVALID_AUTH_HEADER)).thenReturn(INVALID_CLIENT_ID);
    when(authenticationService.isValidClient(INVALID_CLIENT_ID)).thenReturn(false);

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", INVALID_AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verify(authenticationService).extractClientId(INVALID_AUTH_HEADER);
    verify(authenticationService).isValidClient(INVALID_CLIENT_ID);
  }

  @Test
  void testBarEndpoint_RateLimitExceeded_ThrowsException() throws Exception {
    ClientConfiguration config = new ClientConfiguration(15, 8);
    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE)).thenReturn(config);
    when(rateLimitService.allowRequestForBar(VALID_CLIENT_ID_ONE, config)).thenReturn(false);

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Rate limit exceeded"));

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_ONE);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_ONE);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_ONE);
    verify(rateLimitService).allowRequestForBar(VALID_CLIENT_ID_ONE, config);
  }

  @Test
  void testBarEndpoint_MissingAuthorizationHeader_ReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/bar").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("No Authorization header provided"));

    verify(authenticationService, never()).extractClientId(any());
    verify(authenticationService, never()).isValidClient(any());
  }

  @Test
  void testBarEndpoint_MalformedAuthorizationHeader_ReturnsUnauthorized() throws Exception {
    when(authenticationService.extractClientId(MALFORMED_AUTH_HEADER)).thenReturn("");
    when(authenticationService.isValidClient("")).thenReturn(false);

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", MALFORMED_AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verify(authenticationService).extractClientId(MALFORMED_AUTH_HEADER);
    verify(authenticationService).isValidClient("");
  }

  @Test
  void testBothEndpoints_SameClientDifferentConfigurations() throws Exception {
    ClientConfiguration config = new ClientConfiguration(20, 10);
    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE)).thenReturn(config);
    when(rateLimitService.allowRequestForFoo(VALID_CLIENT_ID_ONE, config)).thenReturn(true);
    when(rateLimitService.allowRequestForBar(VALID_CLIENT_ID_ONE, config)).thenReturn(true);

    mockMvc
        .perform(
            get("/foo")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rateLimitService).allowRequestForFoo(VALID_CLIENT_ID_ONE, config);
    verify(rateLimitService).allowRequestForBar(VALID_CLIENT_ID_ONE, config);
  }

  @Test
  void testBothEndpoints_DifferentClientDifferentConfigurations() throws Exception {
    ClientConfiguration configClientOne = new ClientConfiguration(4, 2);
    ClientConfiguration configClientTwo = new ClientConfiguration(5, 3);

    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_ONE))
        .thenReturn(VALID_CLIENT_ID_ONE);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_ONE)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_ONE))
        .thenReturn(configClientOne);
    when(rateLimitService.allowRequestForBar(VALID_CLIENT_ID_ONE, configClientOne))
        .thenReturn(true);

    when(authenticationService.extractClientId(VALID_AUTH_HEADER_CLIENT_TWO))
        .thenReturn(VALID_CLIENT_ID_TWO);
    when(authenticationService.isValidClient(VALID_CLIENT_ID_TWO)).thenReturn(true);
    when(authenticationService.getClientConfiguration(VALID_CLIENT_ID_TWO))
        .thenReturn(configClientTwo);
    when(rateLimitService.allowRequestForBar(VALID_CLIENT_ID_TWO, configClientTwo))
        .thenReturn(false);

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_ONE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true));

    mockMvc
        .perform(
            get("/bar")
                .header("Authorization", VALID_AUTH_HEADER_CLIENT_TWO)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Rate limit exceeded"));

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_ONE);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_ONE);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_ONE);
    verify(rateLimitService).allowRequestForBar(VALID_CLIENT_ID_ONE, configClientOne);

    verify(authenticationService).extractClientId(VALID_AUTH_HEADER_CLIENT_TWO);
    verify(authenticationService).isValidClient(VALID_CLIENT_ID_TWO);
    verify(authenticationService).getClientConfiguration(VALID_CLIENT_ID_TWO);
    verify(rateLimitService).allowRequestForBar(VALID_CLIENT_ID_TWO, configClientTwo);
  }
}

package dev.pafsmith.ledgerflow.common;

import dev.pafsmith.ledgerflow.auth.service.CustomUserDetailsService;
import dev.pafsmith.ledgerflow.auth.service.JwtService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Base class for all @WebMvcTest controller tests.
 *
 * When Spring Security is on the classpath, @WebMvcTest loads SecurityConfig,
 * which depends on JwtAuthenticationFilter → JwtService + CustomUserDetailsService.
 * Declaring those mocks here satisfies the dependency graph for every subclass,
 * so individual tests never need to repeat them.
 */
public abstract class BaseControllerTest {

  @MockitoBean
  protected JwtService jwtService;

  @MockitoBean
  protected CustomUserDetailsService customUserDetailsService;
}

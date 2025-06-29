package de.kaiserpfalzedv.commons.users.client.service;


import de.kaiserpfalzedv.commons.users.domain.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * The authentication manager for APIs.
 *
 * <p>This {@link AuthenticationManager} uses the APIKEY HTTP header to
 * authenticate the user and return the {@link de.kaiserpfalzedv.commons.users.domain.model.user.User} as
 * {@link Authentication}.</p>
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 04.05.2025
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@XSlf4j
public class ApiKeyAuthenticationManager implements AuthenticationManager {
  private final ApplicationEventPublisher bus;
  private final AuthenticationService authenticationService;
  
  // FIXME 2025-06-15 klenkes74 Implement the API key authentication manager.
  
  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    return null;
  }
}

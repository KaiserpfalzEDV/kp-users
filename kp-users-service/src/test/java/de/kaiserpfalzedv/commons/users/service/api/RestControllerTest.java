package de.kaiserpfalzedv.commons.users.service.api;


import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.User;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-21
 */
public class RestControllerTest {
  private R2dbcUserRepository userRepository;
  private WebTestClient sut;
  
  @BeforeEach
  public void setUp() {
    userRepository = mock(R2dbcUserRepository.class);
    sut = WebTestClient.bindToController(new UsersController(userRepository))
        .configureClient()
        .baseUrl("http://localhost:8080/api/v1/users")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build();
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
  }
  
  
  @Test
  public void shouldReturnAnEmptyListWhenNoUsersArePresent() {
    when(userRepository.findAll()).thenReturn(Flux.empty());
    
    sut.get().exchange()
        .expectStatus().isOk()
        .expectBodyList(User.class)
        .hasSize(0);
  }
  
  
  @Test
  public void shouldReturnAListWhenUsersArePresent() {
    when(userRepository.findAll()).thenReturn(
        Flux.just(
            KpUserDetails.builder()
                .issuer("https://issuer.issuer").subject(UUID.randomUUID().toString())
                .nameSpace("./../").name("Test User")
                .email("email@email.email")
                .discord("discord#1234")
                .build()
        )
    );
    
    sut.get().exchange()
        .expectStatus().isOk()
        .expectBodyList(User.class)
        .hasSize(1)
        .consumeWith(response -> {
          User user = response.getResponseBody().get(0);
          assert user != null;
          assert "Test User".equals(user.getName());
        });
  }
}

/*
 * Copyright (c) 2025. Roland T. Lichti, Kaiserpfalz EDV-Service.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.kaiserpfalzedv.commons.users.store.model.user;

import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserToKpUserDetailsImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.*;
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2DbcUserRepositoryDataManagementServiceTest {
  
  @InjectMocks
  private R2dbcUserDataManagementService sut;
  
  @Mock
  private R2dbcUserRepository repository;
  
  @Mock
  private ApplicationEventPublisher bus;
  
  @Mock
  private UserToKpUserDetailsImpl toJpa;
  
  
  @BeforeEach
  public void setUp() {
    reset(bus, repository, toJpa);
  }
  
  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(bus, repository, toJpa);
    validateMockitoUsage();
  }
  
  
  @Test
  void shouldUpdateTheIssuerWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().issuer("new-issuer").subject("new-subject").build()));
    
    sut.updateSubject(DEFAULT_ID, "new-issuer", "new-subject")
        .block();
    
    verify(bus).publishEvent(any(UserSubjectModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenUpdatingTheIssuerWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateSubject(DEFAULT_ID, "new-issuer", "new-subject");


    checkUserNotFoundException(result);
    verify(bus, never()).publishEvent(any(UserSubjectModificationEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateNamespaceWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().nameSpace("new-namespace").build()));
    
    sut.updateNamespace(DEFAULT_ID, "new-namespace").block();
    
    
    verify(bus).publishEvent(any(UserNamespaceModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenUpdatingNamespaceWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateNamespace(DEFAULT_ID, "new-namespace");
    checkUserNotFoundException(result);
    
    verify(bus, never()).publishEvent(any(UserNamespaceModificationEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateNameWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().name("new-name").build()));
    
    sut.updateName(DEFAULT_ID, "new-name").block();
    
    verify(bus).publishEvent(any(UserNameModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenUpdatingNameWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateName(DEFAULT_ID, "new-name");
    checkUserNotFoundException(result);
    
    verify(bus, never()).publishEvent(any(UserNameModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldUpdateNamespaceAndNameWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().name("new-name").nameSpace("new-namespace").build()));
    
    sut.updateNamespaceAndName(DEFAULT_ID, "new-namespace", "new-name").block();
    
    verify(bus).publishEvent(any(UserNamespaceAndNameModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenUpdatingNamespaceAndNameWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateNamespaceAndName(DEFAULT_ID, "new-namespace", "new-name");

    checkUserNotFoundException(result);
    verify(bus, never()).publishEvent(any(UserNamespaceAndNameModificationEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateEmailWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().email("new-email@email.org").build()));
    
    sut.updateEmail(DEFAULT_ID, "new-email@email.org").block();
    
    verify(bus).publishEvent(any(UserEmailModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowAnExceptionWhenUpdatingEmailWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateEmail(DEFAULT_ID, "new-email@email.org");
    
    checkUserNotFoundException(result);
    verify(bus, never()).publishEvent(any(UserEmailModificationEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateDiscordWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().discord("new-discord").build()));
    
    sut.updateDiscord(DEFAULT_ID, "new-discord").block();
    
    verify(bus).publishEvent(any(UserDiscordModificationEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowAnExceptionWhenUpdatingDiscordWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    Mono<KpUserDetails> result = sut.updateDiscord(DEFAULT_ID, "new-discord");
      
    checkUserNotFoundException(result);
    verify(bus, never()).publishEvent(any(UserDiscordModificationEvent.class));
    
    log.exit();
  }
  
  
  
  private static void checkUserNotFoundException(final Mono<KpUserDetails> result) {
    try {
      result.block();
      
      fail("Exception expected");
    } catch (Exception e) {
      log.info("Exception caught: type={}, message={}",
          e.getCause().getClass().getName(),
          e.getCause().getMessage()
      );
      assertTrue(e.getCause().getMessage().startsWith("There is no user with id '"));
    }
  }
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  
  private static final KpUserDetails DEFAULT_JPA_USER = KpUserDetails.builder()
      .id(DEFAULT_ID)
      
      .nameSpace("namespace")
      .name("name")
      
      .issuer("issuer")
      .subject(DEFAULT_ID.toString())
      
      .email("email@email.email")
      
      .created(CREATED_AT)
      .modified(CREATED_AT)
      
      .build();
}

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
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@SuppressWarnings("LoggingSimilarMessage")
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcUserReadServiceTest {
  private R2dbcUserReadService sut;
  
  @Mock
  private R2dbcUserRepository repository;
  
  
  @BeforeEach
  public void setUp() {
    reset(repository);
    
    sut = new R2dbcUserReadService(repository);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
  }
  
  
  @Test
  void shouldReturnUserWhenUserWithIdExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_USER.getId())).thenReturn(Mono.just(DEFAULT_USER));
    
    Optional<KpUserDetails> result = sut.findById(DEFAULT_USER.getId()).blockOptional();
    log.debug("Result. user={}", result.orElse(null));
    
    assertTrue(result.isPresent());
    
    log.exit();
  }
  
  
  @Test
  void shouldReturnUserWhenUserWithUsernameExists() {
    log.entry();
    
    when(repository.findByNameSpaceAndName(DEFAULT_USER.getNameSpace(), DEFAULT_USER.getName())).thenReturn(Mono.just(DEFAULT_USER));
    
    Optional<KpUserDetails> result = sut.findByUsername(DEFAULT_USER.getNameSpace(), DEFAULT_USER.getName()).blockOptional();
    log.debug("Result. user={}", result.orElse(null));
    
    assertTrue(result.isPresent());
    
    log.exit();
  }
  
  
  @Test
  void shouldReturnUserWhenUserWithSubjectExists() {
    log.entry();
    
    when(repository.findByIssuerAndSubject(DEFAULT_USER.getIssuer(), DEFAULT_USER.getSubject())).thenReturn(Mono.just(DEFAULT_USER));
    
    Optional<KpUserDetails> result = sut.findByOauth(DEFAULT_USER.getIssuer(), DEFAULT_USER.getSubject()).blockOptional();
    log.debug("Result. user={}", result.orElse(null));
    
    assertTrue(result.isPresent());
    
    log.exit();
  }
  
  // findAll()
  @Test
  void shouldReturnAllUsersWhenUsersExist() {
    log.entry();
  
    when(repository.findAll()).thenReturn(Flux.just(DEFAULT_USER, DEFAULT_USER));
  
    var result = sut.findAll();
    log.debug("Result: users={}", result);

    assertTrue(result.collectList().blockOptional().orElse(List.of()).contains(DEFAULT_USER));
  
    log.exit();
  }
  
  @Test
  void shouldReturnEmptyListWhenNoUsersExist() {
    log.entry();
  
    when(repository.findAll()).thenReturn(Flux.empty());
  
    var result = sut.findAll();
    log.debug("Result: users={}", result);
  
    assertTrue(result.collectList().blockOptional().isEmpty());
  
    log.exit();
  }
  
  // findByNamespace(String)
  @Test
  void shouldReturnUsersByNamespace() {
    log.entry();
  
    when(repository.findByNameSpace("namespace")).thenReturn(Flux.just(DEFAULT_USER, DEFAULT_USER));
  
    var result = sut.findByNamespace("namespace");
    log.debug("Result: users={}", result);

    assertTrue(result.collectList().blockOptional().orElse(List.of()).contains(DEFAULT_USER));
  
    log.exit();
  }
  
  @Test
  void shouldReturnEmptyListWhenNoUsersInNamespace() {
    log.entry();
  
    when(repository.findByNameSpace("namespace")).thenReturn(Flux.empty());
  
    var result = sut.findByNamespace("namespace");
    log.debug("Result: users={}", result);
  
    assertTrue(result.collectList().blockOptional().isEmpty());
  
    log.exit();
  }
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final KpUserDetails DEFAULT_USER = KpUserDetails.builder()
      .id(DEFAULT_ID)
      
      .nameSpace("namespace")
      .name("name")
      
      .issuer("issuer")
      .subject(DEFAULT_ID.toString())
      
      .created(CREATED_AT)
      .modified(CREATED_AT)
      
      .build();
}

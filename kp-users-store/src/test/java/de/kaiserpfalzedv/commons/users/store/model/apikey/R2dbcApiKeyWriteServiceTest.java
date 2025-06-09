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

package de.kaiserpfalzedv.commons.users.store.model.apikey;


import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyImpl;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.InvalidApiKeyException;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.events.ApiKeyCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.events.ApiKeyRevokedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@SuppressWarnings("LoggingSimilarMessage")
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcApiKeyWriteServiceTest {
  @InjectMocks private R2dbcApiKeyRepository sut;
  @Mock private R2dbcApiKeyInternalRepository repository;
  @Mock private ApplicationEventPublisher bus;
  @Mock private ApiKeyToImpl toImpl;
  
  
  @BeforeEach
  public void setUp() {
    reset(repository, bus, toImpl);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
    verifyNoMoreInteractions(repository, bus, toImpl);
  }
  
  
  @Test
  void shouldSaveApiKeyWhenApiKeyIsNotADuplicate() {
    log.entry();
    
    when(repository.save(DEFAULT_APIKEY)).thenReturn(Mono.just(DEFAULT_APIKEY));
    
    sut.create(DEFAULT_APIKEY).block();
    
    verify(bus, times(1)).publishEvent(any(ApiKeyCreatedEvent.class));
  }
  
  
  @Test
  void shouldThrowExceptionWhenApiKeyIsADuplicate() {
    log.entry();
    
    when(repository.save(DEFAULT_APIKEY)).thenReturn(Mono.error(new IllegalArgumentException("Duplicate key")));
    
    Mono<ApiKeyImpl> result = sut.create(DEFAULT_APIKEY);
    
    try {
      result.block();
      
      fail("Expected InvalidApiKeyException to be thrown");
    } catch (Exception e) {
      log.debug("Caught exception: type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
      
      assertTrue(e.getCause() instanceof InvalidApiKeyException, "Expected InvalidApiKeyException, but got: " + e.getCause().getClass().getName());
    }
    
    
    log.exit();
  }
  
  
  @Test
  void shouldReturnRefreshedKeyWithNewExpiryDateWhenApiKeyGetsRefreshed() {
    log.entry();
    
    when(repository.findById(DEFAULT_APIKEY.getId())).thenReturn(Mono.just(DEFAULT_APIKEY));
    when(repository.save(any(ApiKeyImpl.class)))
        .thenReturn(Mono.just(DEFAULT_APIKEY.toBuilder().expiration(OffsetDateTime.now().plusDays(20L)).build()));
    
    ApiKeyImpl result = sut.refresh(DEFAULT_ID, 20L).block();
    log.debug("Refreshed ApiKey. apikey={}", result);
    
    assertNotNull(result);
    assertTrue(result.getExpiration().plusSeconds(1).isAfter(DEFAULT_APIKEY.getExpiration().plusDays(10L)));
    
    log.exit();
  }
  
  
  @Test
  void shouldFailWhileRefreshingWhenApiKeyWithIdDoesNotExist() {
    log.entry();
    
    when(repository.findById(any(UUID.class))).thenReturn(Mono.empty());
    
    Mono<ApiKeyImpl> result = sut.refresh(DEFAULT_ID, 20L);
    
    try {
      result.block();
      
      fail("Expected ApiKeyNotFoundException to be thrown");
    } catch (Exception e) {
      log.debug("Caught exception: type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
      
      assertTrue(e.getCause() instanceof ApiKeyNotFoundException, "Expected ApiKeyNotFoundException, but got: " + e.getCause().getClass().getName());
    }
    
    log.exit();
  }
  
  
  @Test
  void shouldDeleteByIdApiKeyWhenApiKeyExists() {
    log.entry();
    
    when(repository.deleteById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    sut.deleteById(DEFAULT_APIKEY.getId());
    
    verify(bus, times(1)).publishEvent(any(ApiKeyRevokedEvent.class)); // This is a bit of a hack, but we want to verify the event is published.
    
    log.exit();
  }
  
  
  @Test
  void shouldDeleteByIdApiKeyWhenApiKeyDoesNotExist() {
    log.entry();
    
    when(repository.deleteById(any(UUID.class))).thenReturn(Mono.empty());
    
    sut.deleteById(UUID.randomUUID());
    
    verify(bus, times(1)).publishEvent(any(ApiKeyRevokedEvent.class)); // This is a bit of a hack, but we want to verify the event is published.
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveApiKeyWhenApiKeyExists() {
    log.entry();
    
    when(repository.deleteById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    sut.deleteById(DEFAULT_APIKEY.getId());

    verify(bus, times(1)).publishEvent(any(ApiKeyRevokedEvent.class)); // This is a bit of a hack, but we want to verify the event is published.
    
    log.exit();
  }
  
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime NOW = OffsetDateTime.now();
  
  private static final KpUserDetails DEFAULT_USER = KpUserDetails.builder()
      .id(DEFAULT_ID)
      
      .nameSpace("namespace")
      .name("name")
      
      .issuer("issuer")
      .subject(DEFAULT_ID.toString())
      
      .email("email@email.email")
      
      .created(NOW)
      .modified(NOW)
      
      .build();
  
  private static final ApiKeyImpl DEFAULT_APIKEY = ApiKeyImpl.builder()
      .id(DEFAULT_ID)
      .expiration(NOW.plusDays(10L))
      
      .nameSpace("namespace")
      .user(DEFAULT_USER)
      
      .created(NOW)
      .modified(NOW)
      
      .build();
  }

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
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
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
public class R2dbcApiKeyReadServiceTest {
  @InjectMocks private R2dbcApiKeyRepository sut;
  @Mock private R2dbcApiKeyInternalRepository repository;
  
  @Mock private R2dbcEntityTemplate template;
  @Mock private ReactiveSelectOperation.ReactiveSelect<ApiKeyImpl> reactiveSelect;
  @Mock private ReactiveSelectOperation.SelectWithProjection<ApiKeyImpl> selectWithProjection;
  @Mock private ReactiveSelectOperation.TerminatingSelect<ApiKeyImpl> terminatingSelectOperation;
  
  
  @BeforeEach
  public void setUp() {
    reset(repository, template, reactiveSelect, terminatingSelectOperation);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
    verifyNoMoreInteractions(repository, template);
  }
  
  
  @Test
  void shouldReturnApiKeyWhenApiKeyWithIdExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.justOrEmpty(DEFAULT_APIKEY));
    
    ApiKeyImpl result = sut.retrieve(DEFAULT_ID).block();
    log.debug("Result. apikey={}", result);
    
    assertNotNull(result);
    
    log.exit();
  }
  
  
  @Test
  void shouldReturnApiKeyWhenApiKeyWithIdAsStringExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.justOrEmpty(DEFAULT_APIKEY));
    
    ApiKeyImpl result = sut.retrieve(DEFAULT_ID.toString()).block();
    log.debug("Result. apikey={}", result);
    
    assertNotNull(result);
    
    log.exit();
  }
  
  
  @Test
  void shouldThrowExceptionWhenApiKeyStringIsNoUUID() {
    log.entry();
    
    assertThrows(IllegalArgumentException.class, () -> sut.retrieve("no-uuid").block());
    
    log.exit();
  }
  
  
  @Test
  void shouldReturnListOfApiKeysWhenUserExistsAndHasApiKeys() {
    log.entry();
    
    when(template.select(ApiKeyImpl.class)).thenReturn(reactiveSelect);
    when(reactiveSelect.from("APIKEYS")).thenReturn(selectWithProjection);
    when(selectWithProjection.matching(any())).thenReturn(terminatingSelectOperation);
    when(terminatingSelectOperation.all()).thenReturn(Flux.just(DEFAULT_APIKEY));
    
    List<ApiKeyImpl> result = sut
        .retrieveForUser(DEFAULT_USER.getId())
        .collectList().block();
    log.debug("Result. apikeys={}", result);
    
    assertNotNull(result);
    assertFalse(result.isEmpty());
    
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

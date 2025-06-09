package de.kaiserpfalzedv.commons.users.store.model.apikey;


import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyImpl;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRepository;
import io.r2dbc.spi.Row;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Converter to read an {@link ApiKeyImpl} from a {@link Row} in the R2DBC database.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2025-06-09
 */
@Component
@ReadingConverter
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcApiKeyReadingConverter implements Converter<Row, ApiKeyImpl> {
  private static final Duration TIMEOUT = Duration.ofMillis(250L);
  
  private final R2dbcUserRepository users;
  
  @Override
  public ApiKeyImpl convert(@Nullable final Row source) {
    log.entry(source);
    
    if (source == null) {
      return log.exit(null);
    }

    ApiKeyImpl.ApiKeyImplBuilder result = ApiKeyImpl.builder()
        .id(source.get("id", UUID.class))
        .nameSpace(source.get("namespace", String.class))
        .created(source.get("created", OffsetDateTime.class))
        .expiration(source.get("expires_at", OffsetDateTime.class))
        .modified(source.get("modified", OffsetDateTime.class))
        .deleted(source.get("deleted", OffsetDateTime.class))
        ;
    
    users
        .findById(source.get("user", UUID.class))
        .blockOptional(TIMEOUT)
        .ifPresent(result::user);
    
    return log.exit(result.build());
  }
}

package com.mercari.merpay.pubsub.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wrapper around {@link Properties} to facilitate configurations with injected environment variables.
 */
public class ApplicationProperties extends Properties {

  @Serial
  private static final long serialVersionUID = 5640310164454647178L;
  private static final Logger LOGGER = LogManager.getLogger(ApplicationProperties.class);
  private static final String PROPERTIES_FILENAME = "application.properties";
  private static final Pattern ENVIRONMENT_VARIABLE_PATTERN = Pattern.compile("\\$\\{([a-zA-Z]\\w*)}");

  public ApplicationProperties load() throws IOException {
    InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(PROPERTIES_FILENAME);

    try (in) {
      load(in);
    } catch (IOException e) {
      LOGGER.error("Failed loading properties", e);
      throw e;
    }

    if (isEmpty()) {
      IllegalStateException e = new IllegalStateException("Properties are empty");
      LOGGER.error(e.getMessage(), e);
      throw e;
    }

    return this;
  }

  @Override
  public String getProperty(String key) {
    return parseProperty(super.getProperty(key));
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    String value = super.getProperty(key);
    return (value == null) ? defaultValue : parseProperty(value);
  }

  private static String parseProperty(String property) {
    Matcher matcher = ENVIRONMENT_VARIABLE_PATTERN.matcher(property);
    Map<String, String> replacements = System.getenv();
    StringBuilder builder = new StringBuilder();
    int cursor = 0;
    while (matcher.find()) {
      builder.append(property, cursor, matcher.start());
      String replacement = replacements.get(matcher.group(1));
      builder.append(replacement == null ? matcher.group(0) : replacement);
      cursor = matcher.end();
    }
    builder.append(property.substring(cursor));
    return builder.toString();
  }
}

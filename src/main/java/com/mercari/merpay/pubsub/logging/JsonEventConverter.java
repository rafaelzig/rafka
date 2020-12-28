package com.mercari.merpay.pubsub.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

/**
 * Custom implementation of log4j2's {@link LogEventPatternConverter} in order to produce log events in JSON format.
 */
@ConverterKeys("JsonEventConverter")
@Plugin(name = "JsonEventConverter", category = PatternConverter.CATEGORY)
public class JsonEventConverter extends LogEventPatternConverter {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z")
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());

  /**
   * Private constructor.
   *
   * @param options options, may be null.
   */
  private JsonEventConverter(final String[] options) {
    super(JsonEventConverter.class.getSimpleName(), JsonEventConverter.class.getSimpleName());
  }

  /**
   * Obtains an instance of pattern converter.
   *
   * @param options options, may be null.
   * @return instance of pattern converter.
   */
  public static JsonEventConverter newInstance(String[] options) {
    return new JsonEventConverter(options);
  }

  private static void logException(JsonObject json, Throwable throwable, String prefix, boolean recurse) {
    if (throwable != null) {
      String canonicalName = throwable.getClass().getCanonicalName();
      if (canonicalName != null) {
        json.addProperty(prefix + "_class", canonicalName);
      }
      String message = throwable.getMessage();
      if (message != null) {
        json.addProperty(prefix + "_message", message);
      }

      StringWriter writer = new StringWriter();
      throwable.printStackTrace(new PrintWriter(writer));
      json.addProperty(prefix + "_stack_trace", writer.toString());

      if (recurse) {
        logException(json, throwable.getCause(), "cause", false);
      }
    }
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    JsonObject root = new JsonObject();

    if (event == null) {
      root.addProperty("message", "Logging event registered is null.");
      root.addProperty("severity", "ERROR");
      toAppendTo.append(root);
      return;
    }

    root.addProperty("timestamp", DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeMillis())));
    root.addProperty("message", event.getMessage().getFormattedMessage());
    root.addProperty("severity", event.getLevel().toString());

    JsonArray ndc = new JsonArray();
    event.getContextStack().forEach(ndc::add);
    root.add("ndc", ndc);
    event.getContextData().forEach((key, value) -> addEntry(root, key, value.toString()));

    logException(root, event.getThrown(), "exception", true);

    String loggerName = event.getLoggerName();
    if (loggerName != null) {
      root.addProperty("logger_name", loggerName);
    }

    StackTraceElement source = event.getSource();
    if (source != null) {
      root.addProperty("file", source.getFileName());
      root.addProperty("line_number", source.getLineNumber());
      root.addProperty("class", source.getClassName());
      root.addProperty("method", source.getMethodName());
    }

    String threadName = event.getThreadName();
    if (threadName != null) {
      root.addProperty("thread_name", threadName);
      root.addProperty("thread_id", event.getThreadId());
    }

    toAppendTo.append(root);
  }

  private void addEntry(JsonObject json, String key, String value) {
    try {
      json.addProperty(key, NumberFormat.getInstance().parse(value));
    } catch (ParseException e) {
      json.addProperty(key, value);
    }
  }

  @Override
  public boolean handlesThrowable() {
    return true;
  }
}

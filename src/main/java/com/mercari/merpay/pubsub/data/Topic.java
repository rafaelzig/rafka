package com.mercari.merpay.pubsub.data;

import lombok.NonNull;
import lombok.Value;

/**
 * Represents a topic.
 */
@Value
public class Topic {

  @NonNull String name;
  long position;
}

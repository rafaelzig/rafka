package com.mercari.merpay.pubsub.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a role in the publish/subscriber pattern.
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
  SUBSCRIBER("subscriber_%s"), PUBLISHER("publisher_%s");
  @NonNull
  public final String fileNameFormat;
}

package com.mesofi.mythclothapi.utils;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility methods for normalizing JSON structures in integration tests.
 *
 * <p>This class is intended exclusively for test code. It removes non-deterministic fields (such as
 * database-generated identifiers and timestamps) from JSON payloads to allow stable and repeatable
 * assertions against "golden" expected JSON files.
 *
 * <p>The normalization process:
 *
 * <ul>
 *   <li>Removes root-level timestamp fields ({@code createdAt}, {@code updatedAt})
 *   <li>Recursively removes all {@code id} fields at any depth
 * </ul>
 *
 * <p>The resulting JSON structure preserves semantic content while eliminating values that cannot
 * be reliably asserted in automated tests.
 */
public final class JsonTestUtils {

  /** Prevents instantiation. */
  private JsonTestUtils() {}

  /**
   * Normalizes a JSON tree for deterministic comparison in tests.
   *
   * <p>If the provided node is not an object, the method returns immediately. Otherwise, it removes
   * known volatile fields and recursively strips all {@code id} fields from nested objects and
   * arrays.
   *
   * <p>This method mutates the provided {@link JsonNode} in place.
   *
   * @param node the root JSON node to normalize; may be {@code null}
   */
  public static void normalize(JsonNode node) {
    if (!node.isObject()) {
      return;
    }

    ObjectNode root = (ObjectNode) node;

    // Remove root-level timestamps (non-deterministic)
    root.remove(List.of("createdAt", "updatedAt"));

    // Remove all id fields recursively
    removeAllIds(root);
  }

  /**
   * Recursively removes all {@code id} fields from a JSON tree.
   *
   * <p>This method traverses:
   *
   * <ul>
   *   <li>{@link ObjectNode} instances, removing the {@code id} field if present
   *   <li>arrays, recursively processing each element
   * </ul>
   *
   * <p>Primitive nodes are ignored.
   *
   * @param node the current JSON node being traversed
   */
  private static void removeAllIds(JsonNode node) {
    if (node == null) {
      return;
    }

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;

      // Remove id at the current level
      objectNode.remove("id");

      // Recurse into nested values
      objectNode.elements().forEachRemaining(JsonTestUtils::removeAllIds);

    } else if (node.isArray()) {
      node.forEach(JsonTestUtils::removeAllIds);
    }
  }
}

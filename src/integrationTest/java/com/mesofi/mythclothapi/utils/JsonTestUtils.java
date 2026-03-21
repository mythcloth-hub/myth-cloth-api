package com.mesofi.mythclothapi.utils;

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
 *   <li>Removes root-level and nested {@code id} fields
 *   <li>Removes root-level and nested timestamp fields ({@code createdAt}, {@code updatedAt})
 *   <li>Performs a recursive traversal of objects and arrays
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
   * <p>If the provided node is not an object, the method returns immediately. Otherwise, it
   * recursively removes known volatile fields ({@code id}, {@code createdAt}, {@code updatedAt})
   * from all levels of the JSON structure.
   *
   * <p>This method mutates the provided {@link JsonNode} in place.
   *
   * @param node the root JSON node to normalize; may be {@code null}
   */
  public static void normalize(JsonNode node) {
    if (node == null || !node.isObject()) {
      return;
    }

    ObjectNode root = (ObjectNode) node;

    removeAllOccurrencesOf(root, "id");
    removeAllOccurrencesOf(root, "createdAt");
    removeAllOccurrencesOf(root, "updatedAt");
  }

  /**
   * Recursively removes all occurrences of a given property from a JSON tree.
   *
   * <p>This method traverses:
   *
   * <ul>
   *   <li>{@link ObjectNode} instances, removing the target field if present
   *   <li>Arrays, recursively processing each element
   * </ul>
   *
   * <p>Primitive nodes are ignored.
   *
   * @param node the current JSON node being traversed
   * @param propertyName the field name to remove at any depth
   */
  private static void removeAllOccurrencesOf(JsonNode node, String propertyName) {
    if (node == null) {
      return;
    }

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;

      // Remove id at the current level
      objectNode.remove(propertyName);

      // Recurse into nested values
      objectNode.elements().forEachRemaining(child -> removeAllOccurrencesOf(child, propertyName));

    } else if (node.isArray()) {
      node.forEach(child -> removeAllOccurrencesOf(child, propertyName));
    }
  }
}

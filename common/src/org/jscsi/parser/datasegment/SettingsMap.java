/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SettingsMap.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h1>SettingsMap</h1>
 * <p>
 * This class stores all text parameters and allows an easy access to these.
 * 
 * @author Volker Wildi
 */
public final class SettingsMap {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Logger interface. */
  private static final Log LOGGER = LogFactory.getLog(SettingsMap.class);

  /** Delimiter between the key and the value of a key-value-pair. */
  private static final String KEY_VALUE_DELIMITER = "=";

  /** Delimiter between two key-value-pairs. */
  private static final String PAIR_DELIMITER = "\0";

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** This map contains all settings. */
  private final Map<OperationalTextKey, String> settingsMap;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor to create a new, empty <code>SettingsMap</code> object.
   */
  public SettingsMap() {

    settingsMap = new LinkedHashMap<OperationalTextKey, String>();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Adds a parameter to this <code>SettingsMap</code>.
   * 
   * @param textKey
   *          The name of the parameter to add.
   * @param textValue
   *          The value of the parameter to add.
   */
  public final void add(final OperationalTextKey textKey, final String textValue) {

    if (textKey == null || textValue == null) {
      throw new IllegalArgumentException(
          "This is not a valid operation text key.");
    }

    settingsMap.put(textKey, textValue);
  }

  /**
   * Returns the value of the given parameter, which is not parsed.
   * 
   * @param textKey
   *          The name of the parameter.
   * @return The value of this parameter.
   */
  public final String get(final OperationalTextKey textKey) {

    if (textKey == null) {
      throw new NullPointerException();
    }

    return settingsMap.get(textKey);
  }

  /**
   * Removes the given parameter from this <code>SettingsMap</code>.
   * 
   * @param textKey
   *          The name of the parameter.
   * @return The value of the removed parameter.
   */
  public final String remove(final OperationalTextKey textKey) {

    if (textKey == null) {
      throw new NullPointerException();
    }

    if (!settingsMap.containsKey(textKey)) {
      throw new IllegalArgumentException("This entry does not exists.");
    }

    return settingsMap.remove(textKey);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Update the stored value with the given one. The result is dependant on the
   * <code>IResultFunction</code> instance.
   * 
   * @param textKey
   *          The <code>OperationalTextKey</code> to update.
   * @param newTextValue
   *          The new value, which is interpreted as response.
   * @param resultFunction
   *          The <code>IResultFunction</code> to use to obtaining the result.
   */
  public final void update(final OperationalTextKey textKey,
      final String newTextValue, final IResultFunction resultFunction) {

    final String oldValue = settingsMap.get(textKey);

    String updatedValue;
    if (oldValue == null) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Update old value failed: No old value for key "
            + textKey.value() + ".");
      }

      updatedValue = newTextValue;
    } else {
      updatedValue = resultFunction.result(oldValue, newTextValue);
    }

    settingsMap.put(textKey, updatedValue);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method compares the text parameters of <code>this</code> object with
   * the ones given by a <code>Map</code> object.
   * 
   * @param aSettingsMap
   *          A <code>SettingsMap</code> object, which should be used for the
   *          comparsion.
   * @return <code>true</code>, if the all keys are equal with the ones of the
   *         <code>aSettingsMap</code> object. Else <code>false</code>.
   */
  public final boolean equals(final SettingsMap aSettingsMap) {

    // alias check
    if (this == aSettingsMap) {
      return true;
    }

    if (aSettingsMap != null) {
      return equals(aSettingsMap.settingsMap);
    }

    return false;
  }

  /**
   * This method compares the text parameters of <code>this</code> object with
   * the ones given by a <code>Map</code> object.
   * 
   * @param aMap
   *          A <code>Map&lt;String, String&gt;</code> object, which should be
   *          used for the comparsion.
   * @return <code>true</code>, if the all keys are equal with the ones of the
   *         <code>aMap</code> object. Else <code>false</code>.
   */
  public final boolean equals(final Map<OperationalTextKey, String> aMap) {

    // alias check
    if (this.settingsMap == aMap) {
      return true;
    }

    do {
      if (aMap == null) {
        break;
      }

      if (settingsMap.size() != aMap.size()) {
//        LOGGER.error("The maps have different sizes.");
        break;
      }

      OperationalTextKey key;
      String value;
      for (Map.Entry<OperationalTextKey, String> e : aMap.entrySet()) {

        key = e.getKey();
        value = e.getValue();

        if (!settingsMap.containsKey(key)) {
//          LOGGER.error("KeyValuePair does not contain a key: " + key);

          return false;
        }

        if (settingsMap.get(key).compareTo(value) != 0) {
//          LOGGER
//              .error("KeyValuePair does not pass the compareTo method with key "
//                  + key);
//          LOGGER.error("Value in KeyValuePair: " + settingsMap.get(key));
//          LOGGER.error("Value should be: " + value);

          return false;
        }
      }

      return true;
    } while (false);

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns a set of an key-value pair of this <code>OperationTextKeys</code>
   * object.
   * 
   * @return a set view of the mappings (key-value-pair) contained in this map.
   */
  public final Set<Map.Entry<OperationalTextKey, String>> entrySet() {

    return settingsMap.entrySet();
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {

    return settingsMap.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    final StringBuilder sb = new StringBuilder();
    for (Map.Entry<OperationalTextKey, String> e : settingsMap.entrySet()) {
      sb.append(e.getKey().value());
      sb.append(KEY_VALUE_DELIMITER);
      sb.append(e.getValue());
      sb.append(",");
    }

    return sb.toString();
  }

  /**
   * Removes all stored settings from this <code>SettingsMap</code>.
   */
  public final void clear() {

    settingsMap.clear();
  }

  /**
   * Returns a buffer of the serialized key-value pairs, which are contained in
   * this instance.
   * 
   * @return The serialized key-value pairs.
   */
  public final ByteBuffer asByteBuffer() {

    final StringBuilder sb = new StringBuilder();
    for (Map.Entry<OperationalTextKey, String> e : settingsMap.entrySet()) {
      sb.append(e.getKey().value());
      sb.append(KEY_VALUE_DELIMITER);
      sb.append(e.getValue());
      sb.append(PAIR_DELIMITER);
    }

    return ByteBuffer.wrap(sb.toString().getBytes());
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

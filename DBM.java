/*************************************************************************************************
 * Database manager interface
 *
 * Copyright 2020 Google LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific language governing permissions
 * and limitations under the License.
 *************************************************************************************************/

package tkrzw;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Polymorphic database manager.
 * @note All operations except for Open and Close are thread-safe; Multiple threads can access
 * the same database concurrently.  You can specify a data structure when you call the "open"
 * method.  Every opened database must be closed explicitly by the "close" method to avoid data
 * corruption.  Moreover, every unused database object should be destructed by the "destruct"
 * method to free resources.
 */
public class DBM {
  static {
    Utility.loadLibrary();
  }

  /**
   * Constructor.
   */
  public DBM() {
    initialize();
  }

  /**
   * Initializes the object.
   */
  private native void initialize();

  /**
   * Destructs the object and releases resources.
   * @note The database is closed implicitly if it has not been closed.
   */
  public native void destruct();

  /**
   * Opens a database file.
   * @param path A path of the file.
   * @param writable If true, the file is writable.  If false, it is read-only.
   * @param params Optional parameters.  If it is null, it is ignored.
   * @return The result status.
   * @note The extension of the path indicates the type of the database.
   * <ul>
   * <li>.thh : File hash database (HashDBM)
   * <li>.tkt : File tree database (TreeDBM)
   * <li>.tks : File skip database (SkipDBM)
   * <li>.tkmt : On-memory hash database (TinyDBM)
   * <li>.tkmb : On-memory tree database (BabyDBM)
   * <li>.tkmc : On-memory cache database (CacheDBM)
   * <li>.tksh : On-memory STL hash database (StdHashDBM)
   * <li>.tkst : On-memory STL tree database (StdTreeDBM)
   * </ul>
   * <p>The optional parameters can include options for the file opening operation.
   * <ul>
   * <li>truncate (bool): True to truncate the file.
   * <li>no_create (bool): True to omit file creation.
   * <li>no_wait (bool): True to fail if the file is locked by another process.
   * <li>no_lock (bool): True to omit file locking.
   * </ul>
   * <p>The optional parameter "dbm" supercedes the decision of the database type by the
   * extension.  The value is the type name: "HashDBM", "TreeDBM", "SkipDBM", "TinyDBM",
   * "BabyDBM", "CacheDBM", "StdHashDBM", "StdTreeDBM".
   * <p>For HashDBM, these optional parameters are supported.
   * <ul>
   * <li>update_mode (string): How to update the database file: "UPDATE_IN_PLACE" for the
   * in-palce and "UPDATE_APPENDING" for the appending mode.
   * <li>offset_width (int): The width to represent the offset of records.
   * <li>align_pow (int): The power to align records.
   * <li>num_buckets (int): The number of buckets for hashing.
   * <li>fbp_capacity (int): The capacity of the free block pool.
   * <li>lock_mem_buckets (bool): True to lock the memory for the hash buckets.
   * </ul>
   * <p>For TreeDBM, all optional parameters for HashDBM are available.  In addition, these
   * optional parameters are supported.
   * <ul>
   * <li>max_page_size (int): The maximum size of a page.
   * <li>max_branches (int): The maximum number of branches each inner node can have.
   * <li>max_cached_pages (int): The maximum number of cached pages.
   * <li>key_comparator (string): The comparator of record keys: "LexicalKeyComparator" for
   * the lexical order, "LexicalCaseKeyComparator" for the lexical order ignoring case,
   * "DecimalKeyComparator" for the order of the decimal integer numeric expressions,
   * "HexadecimalKeyComparato" for the order of the hexadecimal integer numeric expressions,
   * "RealNumberKeyComparator" for the order of the decimal real number expressions.
   * </ul>
   * <p>For SkipDBM, these optional parameters are supported.
   * <ul>
   * <li>offset_width (int): The width to represent the offset of records.
   * <li>step_unit (int): The step unit of the skip list.
   * <li>max_level (int): The maximum level of the skip list.
   * <li>sort_mem_size (int): The memory size used for sorting to build the database in the
   * at-random mode.
   * <li>insert_in_order (bool): If true, records are assumed to be inserted in ascending
   * order of the key.
   * <li>max_cached_records (int): The maximum number of cached records.
   * </ul>
   * <p>For TinyDBM, these optional parameters are supported.
   * <ul>
   * <li>num_buckets (int): The number of buckets for hashing.
   * </ul>
   * <p>For BabyDBM, these optional parameters are supported.
   * <ul>
   * <li>key_comparator (string): The comparator of record keys. The same ones as TreeDBM.
   * </ul>
   * <p>For CacheDBM, these optional parameters are supported.
   * <ul>
   * <li>cap_rec_num (int): The maximum number of records.
   * <li>cap_mem_size (int): The total memory size to use.
   * </ul>
   * <p>If the optional parameter "num_shards" is set, the database is sharded into multiple
   * shard files.  Each file has a suffix like "-00003-of-00015".  If the value is 0, the number
   * of shards is set by patterns of the existing files, or 1 if they doesn't exist.
   */
  public native Status open(String path, boolean writable, Map<String, String> params);

  /**
   * Opens a database file, without optional parameters.
   * @param path A path of the file.
   * @param writable If true, the file is writable.  If false, it is read-only.
   * @return The result status.
   */
  public Status open(String path, boolean writable) {
    return open(path, writable, null);
  }

  /**
   * Closes the database file.
   * @return The result status.
   */
  public native Status close();

  /**
   * Gets the value of a record of a key.
   * @param key The key of the record.
   * @param status The status object to store the result status.  If it is null, it is ignored.
   * @return The value data of the record or null on failure.
   */
  public native byte[] get(byte[] key, Status status);

  /**
   * Gets the value of a record of a key, without status assignment.
   * @param key The key of the record.
   * @return The value data of the record or null on failure.
   */
  public byte[] get(byte[] key) {
    return get(key, null);
  }

  /**
   * Gets the value of a record of a key, with string data.
   * @param key The key of the record.
   * @param status The status object to store the result status.  If it is null, it is ignored.
   * @return The value data of the record or null on failure.
   */
  public String get(String key, Status status) {
    byte[] value = get(key.getBytes(StandardCharsets.UTF_8), status);
    if (value == null) {
      return null;
    }
    return new String(value, StandardCharsets.UTF_8);
  }

  /**
   * Gets the value of a record of a key, with string data, without status assignment.
   * @param key The key of the record.
   * @return The value data of the record or null on failure.
   */
  public String get(String key) {
    return get(key, null);
  }

  /**
   * Sets a record of a key and a value.
   * @param key The key of the record.
   * @param value The value of the record.
   * @param overwrite Whether to overwrite the existing value if there's a record with the same
   * key.  If true, the existing value is overwritten by the new value.  If false, the operation
   * is given up and an error status is returned.
   * @return The result status.
   */
  public native Status set(byte[] key, byte[] value, boolean overwrite);

  /**
   * Sets a record of a key and a value, with overwriting.
   * @param key The key of the record.
   * @param value The value of the record.
   * @return The result status.
   */
  public Status set(byte[] key, byte[] value) {
    return set(key, value, true);
  }

  /**
   * Sets a record of a key and a value, with string data.
   * @param key The key of the record.
   * @param value The value of the record.
   * @param overwrite Whether to overwrite the existing value if there's a record with the same
   * key.  If true, the existing value is overwritten by the new value.  If false, the operation
   * is given up and an error status is returned.
   * @return The result status.
   */
  public Status set(String key, String value, boolean overwrite) {
    return set(key.getBytes(StandardCharsets.UTF_8),
               value.getBytes(StandardCharsets.UTF_8), overwrite);
  }

  /**
   * Sets a record of a key and a value, with string data, with overwriting.
   * @param key The key of the record.
   * @param value The value of the record.
   * @return The result status.
   */
  public Status set(String key, String value) {
    return set(key, value, true);
  }

  /**
   * Removes a record of a key.
   * @param key The key of the record.
   * @return The result status.
   */
  public native Status remove(byte[] key);

  /**
   * Removes a record of a key, with string data.
   * @param key The key of the record.
   * @return The result status.
   */
  public Status remove(String key) {
    return remove(key.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Appends data at the end of a record of a key.
   * @param key The key of the record.
   * @param value The value to append.
   * @param delim The delimiter to put after the existing record.
   * @return The result status.
   * @note If there's no existing record, the value is set without the delimiter.
   */
  public native Status append(byte[] key, byte[] value, byte[] delim);

  /**
   * Appends data at the end of a record of a key, with string data.
   * @param key The key of the record.
   * @param value The value to append.
   * @param delim The delimiter to put after the existing record.
   * @return The result status.
   * @note If there's no existing record, the value is set without the delimiter.
   */
  public Status append(String key, String value, String delim) {
    return append(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8),
                  delim.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Compares the value of a record and exchanges if the condition meets.
   * @param key The key of the record.
   * @param expected The expected value.
   * @param desired The desired value.  If it is null, the record is to be removed.
   * @return The result status.
   * @note If the record doesn't exist, NOT_FOUND_ERROR is returned.  If the existing value is
   * different from the expected value, DUPLICATION_ERROR is returned.  Otherwise, the desired
   * value is set.
   */
  public native Status compareExchange(byte[] key, byte[] expected, byte[] desired);

  /**
   * Compares the value of a record and exchanges if the condition meets, with string data.
   * @param key The key of the record.
   * @param expected The expected value.
   * @param desired The desired value.  If it is null, the record is to be removed.
   * @return The result status.
   * @note If the record doesn't exist, NOT_FOUND_ERROR is returned.  If the existing value is
   * different from the expected value, DUPLICATION_ERROR is returned.  Otherwise, the desired
   * value is set.
   */
  public Status compareExchange(String key, String expected, String desired) {
    return compareExchange(key.getBytes(StandardCharsets.UTF_8),
                           expected.getBytes(StandardCharsets.UTF_8),
                           desired == null ? null : desired.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Increments the numeric value of a record.
   * @param key The key of the record.
   * @param inc The incremental value.  If it is Long.MIN_VALUE, the current value is not changed
   * and a new record is not created.
   * @param init The initial value.
   * @param status The status object to store the result status.  If it is null, it is ignored.
   * @return The current value, or Long.MIN_VALUE on vailure
   * @note The record value is stored as an 8-byte big-endian integer.  Negative is also supported.
   */
  public native long increment(byte[] key, long inc, long init, Status status);

  /**
   * Increments the numeric value of a record, with string data.
   * @param key The key of the record.
   * @param inc The incremental value.
   * @param init The initial value.
   * @param status The status object to store the result status.  If it is null, it is ignored.
   * @return The current value, or Long.MIN_VALUE on vailure
   * @note The record value is stored as an 8-byte big-endian integer.  Negative is also supported.
   */
  public long increment(String key, long inc, long init, Status status) {
    return increment(key.getBytes(StandardCharsets.UTF_8), inc, init, status);
  }

  /**
   * Gets the number of records.
   * @return The number of records on success, or -1 on failure.
   */
  public native long count();

  /**
   * Gets the current file size of the database.
   * @return The current file size of the database, or -1 on failure.
   */
  public native long getFileSize();

  /**
   * Gets the path of the database file.
   * @return path The file path of the database, or null on failure.
   */
  public native String getFilePath();

  /**
   * Removes all records.
   * @return The result status.
   */
  public native Status clear();

  /**
   * Rebuilds the entire database.
   * @param params Optional parameters.  If it is null, it is ignored.
   * @return The result status.
   * @note The optional parameters are the same as the Open method.  Omitted tuning parameters
   * are kept the same or implicitly optimized.  If it is null, it is ignored.
   */
  public native Status rebuild(Map<String, String> params);

  /**
   * Rebuilds the entire database, without optional parameters.
   * @return The result status.
   */
  public Status rebuild() {
    return rebuild(null);
  }

  /**
   * Checks whether the database should be rebuilt.
   * @return True to be optimized or false with no necessity.
   */
  public native boolean shouldBeRebuilt();

  /**
   * Synchronizes the content of the database to the file system.
   * @param hard True to do physical synchronization with the hardware or false to do only
   * logical synchronization with the file system.
   * @param params Optional parameters.  If it is null, it is ignored.
   * @return The result status.
   * @note Only SkipDBM uses the optional parameters.  The "merge" parameter specifies paths
   * of databases to merge, separated by colon.  The "reducer" parameter specifies the reducer
   * to apply to records of the same key.  "ReduceToFirst", "ReduceToSecond", "ReduceToLast",
   * etc are supported.
   */
  public native Status synchronize(boolean hard, Map<String, String> params);

  /**
   * Synchronizes the content of the database to the file system.
   * @param hard True to do physical synchronization with the hardware or false to do only
   * logical synchronization with the file system.
   * @return The result status.
   */
  public Status synchronize(boolean hard) {
    return synchronize(hard, null);
  }

  /**
   * Copies the content of the database file to another file.
   * @param dest_path A path to the destination file.
   * @return The result status.
   */
  public native Status copyFile(String dest_path);

  /**
   * Exports all records to another database.
   * @param dest_dbm The destination database.
   * @return The result status.
   */
  public native Status export(DBM dest_dbm);

  /**
   * Exports the keys of all records as lines to a text file.
   * @param dest_path A path of the output text file.
   * @return The result status.
   */
  public native Status exportKeysAsLines(String dest_path);

  /**
   * Inspects the database.
   * @return A map of property names and their values.
   */
  public native Map<String, String> inspect();

  /**
   * Checks whether the database is open.
   * @return True if the database is open, or false if not.
   */
  public native boolean isOpen();

  /**
   * Checks whether the database condition is healthy.
   * @return True if the database condition is healthy, or false if not.
   */
  public native boolean isHealthy();

  /**
   * Checks whether ordered operations are supported.
   * @return True if ordered operations are supported, or false if not.
   */
  public native boolean isOrdered();

  /**
   * Searches the database and get keys which match a pattern.
   * @param mode The search mode.  "contain" extracts keys containing the pattern.  "begin"
   * extracts keys beginning with the pattern.  "end" extracts keys ending with the pattern.
   * "regex" extracts keys partially matches the pattern of a regular expression.  "edit"
   * extracts keys whose edit distance to the pattern is the least.
   * @param pattern The pattern for matching.
   * @param capacity The maximum records to obtain.  0 means unlimited.
   * @param utf If true, text is treated as UTF-8, which affects "regex" and "edit".
   * @return An array of keys matching the condition.
   */
  public native byte[][] search(
      String mode, byte[] pattern, int capacity, boolean utf);

  /**
   * Searches the database and get keys which match a pattern, with string data.
   * @param mode The search mode.  "contain" extracts keys containing the pattern.  "begin"
   * extracts keys beginning with the pattern.  "end" extracts keys ending with the pattern.
   * "regex" extracts keys partially matches the pattern of a regular expression.  "edit"
   * extracts keys whose edit distance to the pattern is the least.
   * @param pattern The pattern for matching.
   * @param capacity The maximum records to obtain.  0 means unlimited.
   * @param utf If true, text is treated as UTF-8, which affects "regex" and "edit".
   * @return An array of keys matching the condition.
   */
  public String[] search(String mode, String pattern, int capacity, boolean utf){
    byte[][] keys = search(mode, pattern.getBytes(StandardCharsets.UTF_8), capacity, utf);
    String[] str_keys = new String[keys.length];
    for (int i = 0; i < keys.length; i++) {
      str_keys[i] = new String(keys[i], StandardCharsets.UTF_8);
    }
    return str_keys;
  }

  /**
   * Makes an iterator for each record.
   * @return The iterator for each record.
   */
  public native Iterator makeIterator();

  /**
   * Gets a string representation of the database.
   */
  public native String toString();

  /** The pointer to the native object */
  private long ptr_ = 0;
}

// END OF FILE

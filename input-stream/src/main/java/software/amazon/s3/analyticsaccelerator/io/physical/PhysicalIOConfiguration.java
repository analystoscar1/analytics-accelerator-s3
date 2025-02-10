/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.amazon.s3.analyticsaccelerator.io.physical;

import static software.amazon.s3.analyticsaccelerator.util.Constants.ONE_KB;
import static software.amazon.s3.analyticsaccelerator.util.Constants.ONE_MB;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import software.amazon.s3.analyticsaccelerator.common.ConnectorConfiguration;
import software.amazon.s3.analyticsaccelerator.common.Preconditions;
import software.amazon.s3.analyticsaccelerator.io.physical.prefetcher.SequentialReadProgression;

/** Configuration for {@link PhysicalIO} */
@Getter
@Builder
@EqualsAndHashCode
public class PhysicalIOConfiguration {
  private static final long DEFAULT_MAX_MEMORY_LIMIT_AAL = Long.MAX_VALUE;
  private static final int DEFAULT_CAPACITY_METADATA_STORE = 50;
  private static final boolean DEFAULT_USE_SINGLE_CACHE = true;
  private static final long DEFAULT_BLOCK_SIZE_BYTES = 8 * ONE_MB;
  private static final long DEFAULT_READ_AHEAD_BYTES = 64 * ONE_KB;
  private static final long DEFAULT_MAX_RANGE_SIZE = 8 * ONE_MB;
  private static final long DEFAULT_PART_SIZE = 8 * ONE_MB;
  private static final double DEFAULT_SEQUENTIAL_PREFETCH_BASE = 2.0;
  private static final double DEFAULT_SEQUENTIAL_PREFETCH_SPEED = 1.0;

  /**
   * Max memory to be used by library. {@link PhysicalIOConfiguration#DEFAULT_MAX_MEMORY_LIMIT_AAL}
   * by default.
   */
  @Builder.Default private long maxMemoryLimitAAL = DEFAULT_MAX_MEMORY_LIMIT_AAL;

  private static final String MAX_MEMORY_LIMIT_AAL_KEY = "aal.maxmemory";

  /**
   * Capacity, in blobs. {@link PhysicalIOConfiguration#DEFAULT_CAPACITY_METADATA_STORE} by default.
   */
  @Builder.Default private int metadataStoreCapacity = DEFAULT_CAPACITY_METADATA_STORE;

  private static final String METADATA_STORE_CAPACITY_KEY = "metadatastore.capacity";

  /** Block size, in bytes. {@link PhysicalIOConfiguration#DEFAULT_BLOCK_SIZE_BYTES} by default. */
  @Builder.Default private long blockSizeBytes = DEFAULT_BLOCK_SIZE_BYTES;

  private static final String BLOCK_SIZE_BYTES_KEY = "blocksizebytes";

  /** Read ahead, in bytes. {@link PhysicalIOConfiguration#DEFAULT_BLOCK_SIZE_BYTES} by default. */
  @Builder.Default private long readAheadBytes = DEFAULT_READ_AHEAD_BYTES;

  private static final String READ_AHEAD_BYTES_KEY = "readaheadbytes";

  /**
   * Maximum range size, in bytes. {@link PhysicalIOConfiguration#DEFAULT_MAX_RANGE_SIZE} by
   * default.
   */
  @Builder.Default private long maxRangeSizeBytes = DEFAULT_MAX_RANGE_SIZE;

  private static final String MAX_RANGE_SIZE_BYTES_KEY = "maxrangesizebytes";

  /** Part size, in bytes. {@link PhysicalIOConfiguration#DEFAULT_PART_SIZE} by default. */
  @Builder.Default private long partSizeBytes = DEFAULT_PART_SIZE;

  private static final String PART_SIZE_BYTES_KEY = "partsizebytes";

  /**
   * Base constant in the sequential prefetching geometric progression. See {@link
   * SequentialReadProgression} for the exact formula. {@link
   * PhysicalIOConfiguration#DEFAULT_SEQUENTIAL_PREFETCH_BASE} by default.
   */
  @Builder.Default private double sequentialPrefetchBase = DEFAULT_SEQUENTIAL_PREFETCH_BASE;

  private static final String SEQUENTIAL_PREFETCH_BASE_KEY = "sequentialprefetch.base";

  /**
   * Constant controlling the rate of physical block _growth_ in the sequential prefetching
   * geometric progression. See {@link SequentialReadProgression} for the exact formula. {@link
   * PhysicalIOConfiguration#DEFAULT_SEQUENTIAL_PREFETCH_SPEED} by default.
   */
  @Builder.Default private double sequentialPrefetchSpeed = DEFAULT_SEQUENTIAL_PREFETCH_SPEED;

  private static final String SEQUENTIAL_PREFETCH_SPEED_KEY = "sequentialprefetch.speed";

  /** Default set of settings for {@link PhysicalIO} */
  public static final PhysicalIOConfiguration DEFAULT = PhysicalIOConfiguration.builder().build();

  /**
   * Constructs {@link PhysicalIOConfiguration} from {@link ConnectorConfiguration} object.
   *
   * @param configuration Configuration object to generate PhysicalIOConfiguration from
   * @return PhysicalIOConfiguration
   */
  public static PhysicalIOConfiguration fromConfiguration(ConnectorConfiguration configuration) {
    return PhysicalIOConfiguration.builder()
        .maxMemoryLimitAAL(
            configuration.getLong(MAX_MEMORY_LIMIT_AAL_KEY, DEFAULT_MAX_MEMORY_LIMIT_AAL))
        .metadataStoreCapacity(
            configuration.getInt(METADATA_STORE_CAPACITY_KEY, DEFAULT_CAPACITY_METADATA_STORE))
        .blockSizeBytes(configuration.getLong(BLOCK_SIZE_BYTES_KEY, DEFAULT_BLOCK_SIZE_BYTES))
        .readAheadBytes(configuration.getLong(READ_AHEAD_BYTES_KEY, DEFAULT_READ_AHEAD_BYTES))
        .maxRangeSizeBytes(configuration.getLong(MAX_RANGE_SIZE_BYTES_KEY, DEFAULT_MAX_RANGE_SIZE))
        .partSizeBytes(configuration.getLong(PART_SIZE_BYTES_KEY, DEFAULT_PART_SIZE))
        .sequentialPrefetchBase(
            configuration.getDouble(SEQUENTIAL_PREFETCH_BASE_KEY, DEFAULT_SEQUENTIAL_PREFETCH_BASE))
        .sequentialPrefetchSpeed(
            configuration.getDouble(
                SEQUENTIAL_PREFETCH_SPEED_KEY, DEFAULT_SEQUENTIAL_PREFETCH_SPEED))
        .build();
  }

  /**
   * Constructs {@link PhysicalIOConfiguration}.
   *
   * @param maxMemoryLimitAAL The memory limit for library
   * @param metadataStoreCapacity The capacity of the MetadataStore
   * @param blockSizeBytes Block size, in bytes
   * @param readAheadBytes Read ahead, in bytes
   * @param maxRangeSizeBytes Maximum physical read issued against the object store
   * @param partSizeBytes What part size to use when splitting up logical reads
   * @param sequentialPrefetchBase Scale factor to control the size of sequentially prefetched
   *     physical blocks. Example: A constant of 2.0 means doubling the block sizes.
   * @param sequentialPrefetchSpeed Constant controlling the rate of growth of sequentially
   *     prefetched physical blocks.
   */
  @Builder
  private PhysicalIOConfiguration(
      long maxMemoryLimitAAL,
      int metadataStoreCapacity,
      long blockSizeBytes,
      long readAheadBytes,
      long maxRangeSizeBytes,
      long partSizeBytes,
      double sequentialPrefetchBase,
      double sequentialPrefetchSpeed) {
    Preconditions.checkArgument(maxMemoryLimitAAL > 0, "`maxMemoryLimitAAL` must be positive");
    Preconditions.checkArgument(
        metadataStoreCapacity > 0, "`metadataStoreCapacity` must be positive");
    Preconditions.checkArgument(blockSizeBytes > 0, "`blockSizeBytes` must be positive");
    Preconditions.checkArgument(readAheadBytes > 0, "`readAheadLengthBytes` must be positive");
    Preconditions.checkArgument(maxRangeSizeBytes > 0, "`maxRangeSize` must be positive");
    Preconditions.checkArgument(partSizeBytes > 0, "`partSize` must be positive");
    Preconditions.checkArgument(
        sequentialPrefetchBase > 0, "`sequentialPrefetchBase` must be positive");
    Preconditions.checkArgument(
        sequentialPrefetchSpeed > 0, "`sequentialPrefetchSpeed` must be positive");

    this.maxMemoryLimitAAL = maxMemoryLimitAAL;
    this.metadataStoreCapacity = metadataStoreCapacity;
    this.blockSizeBytes = blockSizeBytes;
    this.readAheadBytes = readAheadBytes;
    this.maxRangeSizeBytes = maxRangeSizeBytes;
    this.partSizeBytes = partSizeBytes;
    this.sequentialPrefetchBase = sequentialPrefetchBase;
    this.sequentialPrefetchSpeed = sequentialPrefetchSpeed;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append("PhysicalIO configuration:\n");
    builder.append("\tmaxMemoryLimitAAL: " + maxMemoryLimitAAL + "\n");
    builder.append("\tmetadataStoreCapacity: " + metadataStoreCapacity + "\n");
    builder.append("\tblockSizeBytes: " + blockSizeBytes + "\n");
    builder.append("\treadAheadBytes: " + readAheadBytes + "\n");
    builder.append("\tmaxRangeSizeBytes: " + maxRangeSizeBytes + "\n");
    builder.append("\tpartSizeBytes: " + partSizeBytes + "\n");
    builder.append("\tsequentialPrefetchBase: " + sequentialPrefetchBase + "\n");
    builder.append("\tsequentialPrefetchSpeed: " + sequentialPrefetchSpeed + "\n");

    return builder.toString();
  }
}

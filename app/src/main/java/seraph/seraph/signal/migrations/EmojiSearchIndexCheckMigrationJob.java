package seraph.zion.signal.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.SqlUtil;
import seraph.zion.signal.database.EmojiSearchTable;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.jobmanager.Job;
import seraph.zion.signal.jobs.DownloadLatestEmojiDataJob;
import seraph.zion.signal.jobs.EmojiSearchIndexDownloadJob;
import seraph.zion.signal.keyvalue.SignalStore;

/**
 * Schedules job to get the latest emoji search index if it's empty.
 */
public final class EmojiSearchIndexCheckMigrationJob extends MigrationJob {

  public static final String KEY = "EmojiSearchIndexCheckMigrationJob";

  EmojiSearchIndexCheckMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private EmojiSearchIndexCheckMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() {
    if (SqlUtil.isEmpty(SignalDatabase.getRawDatabase(), EmojiSearchTable.TABLE_NAME)) {
      SignalStore.emojiValues().clearSearchIndexMetadata();
      EmojiSearchIndexDownloadJob.scheduleImmediately();
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<EmojiSearchIndexCheckMigrationJob> {
    @Override
    public @NonNull EmojiSearchIndexCheckMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new EmojiSearchIndexCheckMigrationJob(parameters);
    }
  }
}

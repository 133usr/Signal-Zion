package seraph.zion.signal.jobmanager;

import androidx.annotation.NonNull;

import seraph.zion.signal.jobmanager.persistence.JobSpec;

public interface JobPredicate {
  JobPredicate NONE = jobSpec -> true;

  boolean shouldRun(@NonNull JobSpec jobSpec);
}

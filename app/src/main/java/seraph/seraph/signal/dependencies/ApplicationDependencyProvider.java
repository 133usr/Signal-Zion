package seraph.zion.signal.dependencies;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.signal.core.util.ThreadUtil;
import org.signal.core.util.concurrent.DeadlockDetector;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import seraph.zion.signal.BuildConfig;
import seraph.zion.signal.components.TypingStatusRepository;
import seraph.zion.signal.components.TypingStatusSender;
import seraph.zion.signal.crypto.ReentrantSessionLock;
import seraph.zion.signal.crypto.storage.SignalBaseIdentityKeyStore;
import seraph.zion.signal.crypto.storage.SignalIdentityKeyStore;
import seraph.zion.signal.crypto.storage.SignalKyberPreKeyStore;
import seraph.zion.signal.crypto.storage.SignalSenderKeyStore;
import seraph.zion.signal.crypto.storage.SignalServiceAccountDataStoreImpl;
import seraph.zion.signal.crypto.storage.SignalServiceDataStoreImpl;
import seraph.zion.signal.crypto.storage.TextSecurePreKeyStore;
import seraph.zion.signal.crypto.storage.TextSecureSessionStore;
import seraph.zion.signal.database.DatabaseObserver;
import seraph.zion.signal.database.JobDatabase;
import seraph.zion.signal.database.PendingRetryReceiptCache;
import seraph.zion.signal.jobmanager.JobManager;
import seraph.zion.signal.jobmanager.JobMigrator;
import seraph.zion.signal.jobmanager.impl.FactoryJobPredicate;
import seraph.zion.signal.jobs.FastJobStorage;
import seraph.zion.signal.jobs.GroupCallUpdateSendJob;
import seraph.zion.signal.jobs.IndividualSendJob;
import seraph.zion.signal.jobs.JobManagerFactories;
import seraph.zion.signal.jobs.MarkerJob;
import seraph.zion.signal.jobs.PreKeysSyncJob;
import seraph.zion.signal.jobs.PushGroupSendJob;
import seraph.zion.signal.jobs.PushProcessMessageJob;
import seraph.zion.signal.jobs.ReactionSendJob;
import seraph.zion.signal.jobs.TypingSendJob;
import seraph.zion.signal.keyvalue.SignalStore;
import seraph.zion.signal.megaphone.MegaphoneRepository;
import seraph.zion.signal.messages.IncomingMessageObserver;
import seraph.zion.signal.net.SignalWebSocketHealthMonitor;
import seraph.zion.signal.notifications.MessageNotifier;
import seraph.zion.signal.notifications.OptimizedMessageNotifier;
import seraph.zion.signal.payments.MobileCoinConfig;
import seraph.zion.signal.payments.Payments;
import seraph.zion.signal.push.SecurityEventListener;
import seraph.zion.signal.push.SignalServiceNetworkAccess;
import seraph.zion.signal.recipients.LiveRecipientCache;
import seraph.zion.signal.revealable.ViewOnceMessageManager;
import seraph.zion.signal.service.DeletedCallEventManager;
import seraph.zion.signal.service.ExpiringMessageManager;
import seraph.zion.signal.service.ExpiringStoriesManager;
import seraph.zion.signal.service.PendingRetryReceiptManager;
import seraph.zion.signal.service.ScheduledMessageManager;
import seraph.zion.signal.service.TrimThreadsByDateManager;
import seraph.zion.signal.service.webrtc.SignalCallManager;
import seraph.zion.signal.shakereport.ShakeToReport;
import seraph.zion.signal.stories.Stories;
import seraph.zion.signal.util.AlarmSleepTimer;
import seraph.zion.signal.util.AppForegroundObserver;
import seraph.zion.signal.util.ByteUnit;
import seraph.zion.signal.util.EarlyMessageCache;
import seraph.zion.signal.util.FeatureFlags;
import seraph.zion.signal.util.FrameRateTracker;
import seraph.zion.signal.util.TextSecurePreferences;
import seraph.zion.signal.video.exo.GiphyMp4Cache;
import seraph.zion.signal.video.exo.SimpleExoPlayerPool;
import seraph.zion.signal.webrtc.audio.AudioManagerCompat;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceDataStore;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.SignalWebSocket;
import org.whispersystems.signalservice.api.groupsv2.ClientZkOperations;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.push.ServiceId.ACI;
import org.whispersystems.signalservice.api.push.ServiceId.PNI;
import org.whispersystems.signalservice.api.services.CallLinksService;
import org.whispersystems.signalservice.api.services.DonationsService;
import org.whispersystems.signalservice.api.services.ProfileService;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.api.util.SleepTimer;
import org.whispersystems.signalservice.api.util.UptimeSleepTimer;
import org.whispersystems.signalservice.api.websocket.WebSocketFactory;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Implementation of {@link ApplicationDependencies.Provider} that provides real app dependencies.
 */
public class ApplicationDependencyProvider implements ApplicationDependencies.Provider {

  private final Application context;

  public ApplicationDependencyProvider(@NonNull Application context) {
    this.context = context;
  }

  private @NonNull ClientZkOperations provideClientZkOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return ClientZkOperations.create(signalServiceConfiguration);
  }

  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new GroupsV2Operations(provideClientZkOperations(signalServiceConfiguration), FeatureFlags.groupLimits().getHardLimit());
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new SignalServiceAccountManager(signalServiceConfiguration,
                                           new DynamicCredentialsProvider(),
                                           BuildConfig.SIGNAL_AGENT,
                                           groupsV2Operations,
                                           FeatureFlags.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender(@NonNull SignalWebSocket signalWebSocket, @NonNull SignalServiceDataStore protocolStore, @NonNull SignalServiceConfiguration signalServiceConfiguration) {
      return new SignalServiceMessageSender(signalServiceConfiguration,
                                            new DynamicCredentialsProvider(),
                                            protocolStore,
                                            ReentrantSessionLock.INSTANCE,
                                            BuildConfig.SIGNAL_AGENT,
                                            signalWebSocket,
                                            Optional.of(new SecurityEventListener(context)),
                                            provideGroupsV2Operations(signalServiceConfiguration).getProfileOperations(),
                                            SignalExecutors.newCachedBoundedExecutor("signal-messages", ThreadUtil.PRIORITY_IMPORTANT_BACKGROUND_THREAD, 1, 16, 30),
                                            ByteUnit.KILOBYTES.toBytes(256),
                                            FeatureFlags.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new SignalServiceMessageReceiver(signalServiceConfiguration,
                                            new DynamicCredentialsProvider(),
                                            BuildConfig.SIGNAL_AGENT,
                                            provideGroupsV2Operations(signalServiceConfiguration).getProfileOperations(),
                                            FeatureFlags.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return new SignalServiceNetworkAccess(context);
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return new LiveRecipientCache(context);
  }

  @Override
  public @NonNull JobManager provideJobManager() {
    JobManager.Configuration config = new JobManager.Configuration.Builder()
                                                                  .setJobFactories(JobManagerFactories.getJobFactories(context))
                                                                  .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
                                                                  .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
                                                                  .setJobStorage(new FastJobStorage(JobDatabase.getInstance(context)))
                                                                  .setJobMigrator(new JobMigrator(TextSecurePreferences.getJobManagerVersion(context), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(context)))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(PushProcessMessageJob.KEY, MarkerJob.KEY))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(IndividualSendJob.KEY, PushGroupSendJob.KEY, ReactionSendJob.KEY, TypingSendJob.KEY, GroupCallUpdateSendJob.KEY))
                                                                  .build();
    return new JobManager(context, config);
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return new FrameRateTracker(context);
  }

  @SuppressLint("DiscouragedApi")
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return new MegaphoneRepository(context);
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return new EarlyMessageCache();
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return new OptimizedMessageNotifier(context);
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver() {
    return new IncomingMessageObserver(context);
  }

  @Override
  public @NonNull TrimThreadsByDateManager provideTrimThreadsByDateManager() {
    return new TrimThreadsByDateManager(context);
  }

  @Override
  public @NonNull ViewOnceMessageManager provideViewOnceMessageManager() {
    return new ViewOnceMessageManager(context);
  }

  @Override
  public @NonNull ExpiringStoriesManager provideExpiringStoriesManager() {
    return new ExpiringStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringMessageManager provideExpiringMessageManager() {
    return new ExpiringMessageManager(context);
  }

  @Override
  public @NonNull DeletedCallEventManager provideDeletedCallEventManager() {
    return new DeletedCallEventManager(context);
  }

  @Override
  public @NonNull ScheduledMessageManager provideScheduledMessageManager() {
    return new ScheduledMessageManager(context);
  }

  @Override
  public @NonNull TypingStatusRepository provideTypingStatusRepository() {
    return new TypingStatusRepository();
  }

  @Override
  public @NonNull TypingStatusSender provideTypingStatusSender() {
    return new TypingStatusSender();
  }

  @Override
  public @NonNull DatabaseObserver provideDatabaseObserver() {
    return new DatabaseObserver(context);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public @NonNull Payments providePayments(@NonNull SignalServiceAccountManager signalServiceAccountManager) {
    MobileCoinConfig network;

    if      (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("mainnet")) network = MobileCoinConfig.getMainNet(signalServiceAccountManager);
    else if (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("testnet")) network = MobileCoinConfig.getTestNet(signalServiceAccountManager);
    else throw new AssertionError("Unknown network " + BuildConfig.MOBILE_COIN_ENVIRONMENT);

    return new Payments(network);
  }

  @Override
  public @NonNull ShakeToReport provideShakeToReport() {
    return new ShakeToReport(context);
  }

  @Override
  public @NonNull AppForegroundObserver provideAppForegroundObserver() {
    return new AppForegroundObserver();
  }

  @Override
  public @NonNull SignalCallManager provideSignalCallManager() {
    return new SignalCallManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptManager providePendingRetryReceiptManager() {
    return new PendingRetryReceiptManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptCache providePendingRetryReceiptCache() {
    return new PendingRetryReceiptCache();
  }

  @Override
  public @NonNull SignalWebSocket provideSignalWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier) {
    SleepTimer                   sleepTimer      = !SignalStore.account().isFcmEnabled() || SignalStore.internalValues().isWebsocketModeForced() ? new AlarmSleepTimer(context) : new UptimeSleepTimer() ;
    SignalWebSocketHealthMonitor healthMonitor   = new SignalWebSocketHealthMonitor(context, sleepTimer);
    SignalWebSocket              signalWebSocket = new SignalWebSocket(provideWebSocketFactory(signalServiceConfigurationSupplier, healthMonitor));

    healthMonitor.monitor(signalWebSocket);

    return signalWebSocket;
  }

  @Override
  public @NonNull SignalServiceDataStoreImpl provideProtocolStore() {
    ACI localAci = SignalStore.account().getAci();
    PNI localPni = SignalStore.account().getPni();

    if (localAci == null) {
      throw new IllegalStateException("No ACI set!");
    }

    if (localPni == null) {
      throw new IllegalStateException("No PNI set!");
    }

    boolean needsPreKeyJob = false;

    if (!SignalStore.account().hasAciIdentityKey()) {
      SignalStore.account().generateAciIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (!SignalStore.account().hasPniIdentityKey()) {
      SignalStore.account().generatePniIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (needsPreKeyJob) {
      PreKeysSyncJob.enqueueIfNeeded();
    }

    SignalBaseIdentityKeyStore baseIdentityStore = new SignalBaseIdentityKeyStore(context);

    SignalServiceAccountDataStoreImpl aciStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localAci),
                                                                                       new SignalKyberPreKeyStore(localAci),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getAciIdentityKey()),
                                                                                       new TextSecureSessionStore(localAci),
                                                                                       new SignalSenderKeyStore(context));

    SignalServiceAccountDataStoreImpl pniStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localPni),
                                                                                       new SignalKyberPreKeyStore(localPni),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getPniIdentityKey()),
                                                                                       new TextSecureSessionStore(localPni),
                                                                                       new SignalSenderKeyStore(context));
    return new SignalServiceDataStoreImpl(context, aciStore, pniStore);
  }

  @Override
  public @NonNull GiphyMp4Cache provideGiphyMp4Cache() {
    return new GiphyMp4Cache(ByteUnit.MEGABYTES.toBytes(16));
  }

  @Override
  public @NonNull SimpleExoPlayerPool provideExoPlayerPool() {
    return new SimpleExoPlayerPool(context);
  }

  @Override
  public @NonNull AudioManagerCompat provideAndroidCallAudioManager() {
    return AudioManagerCompat.create(context);
  }

  @Override
  public @NonNull DonationsService provideDonationsService(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new DonationsService(signalServiceConfiguration,
                                new DynamicCredentialsProvider(),
                                BuildConfig.SIGNAL_AGENT,
                                groupsV2Operations,
                                FeatureFlags.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull CallLinksService provideCallLinksService(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new CallLinksService(signalServiceConfiguration,
                                new DynamicCredentialsProvider(),
                                BuildConfig.SIGNAL_AGENT,
                                groupsV2Operations,
                                FeatureFlags.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull ProfileService provideProfileService(@NonNull ClientZkProfileOperations clientZkProfileOperations,
                                                       @NonNull SignalServiceMessageReceiver receiver,
                                                       @NonNull SignalWebSocket signalWebSocket)
  {
    return new ProfileService(clientZkProfileOperations, receiver, signalWebSocket);
  }

  @Override
  public @NonNull DeadlockDetector provideDeadlockDetector() {
    HandlerThread handlerThread = new HandlerThread("signal-DeadlockDetector", ThreadUtil.PRIORITY_BACKGROUND_THREAD);
    handlerThread.start();
    return new DeadlockDetector(new Handler(handlerThread.getLooper()), TimeUnit.SECONDS.toMillis(5));
  }

  @Override
  public @NonNull ClientZkReceiptOperations provideClientZkReceiptOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return provideClientZkOperations(signalServiceConfiguration).getReceiptOperations();
  }

  @NonNull WebSocketFactory provideWebSocketFactory(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull SignalWebSocketHealthMonitor healthMonitor) {
    return new WebSocketFactory() {
      @Override
      public WebSocketConnection createWebSocket() {
        return new WebSocketConnection("normal",
                                       signalServiceConfigurationSupplier.get(),
                                       Optional.of(new DynamicCredentialsProvider()),
                                       BuildConfig.SIGNAL_AGENT,
                                       healthMonitor,
                                       Stories.isFeatureEnabled());
      }

      @Override
      public WebSocketConnection createUnidentifiedWebSocket() {
        return new WebSocketConnection("unidentified",
                                       signalServiceConfigurationSupplier.get(),
                                       Optional.empty(),
                                       BuildConfig.SIGNAL_AGENT,
                                       healthMonitor,
                                       Stories.isFeatureEnabled());
      }
    };
  }

  @VisibleForTesting
  static class DynamicCredentialsProvider implements CredentialsProvider {

    @Override
    public ACI getAci() {
      return SignalStore.account().getAci();
    }

    @Override
    public PNI getPni() {
      return SignalStore.account().getPni();
    }

    @Override
    public String getE164() {
      return SignalStore.account().getE164();
    }

    @Override
    public String getPassword() {
      return SignalStore.account().getServicePassword();
    }

    @Override
    public int getDeviceId() {
      return SignalStore.account().getDeviceId();
    }
  }
}

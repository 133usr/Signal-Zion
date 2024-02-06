package seraph.zion.signal.jobs;

import android.app.Application;

import androidx.annotation.NonNull;

import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.jobmanager.Constraint;
import seraph.zion.signal.jobmanager.ConstraintObserver;
import seraph.zion.signal.jobmanager.Job;
import seraph.zion.signal.jobmanager.JobMigration;
import seraph.zion.signal.jobmanager.impl.AutoDownloadEmojiConstraint;
import seraph.zion.signal.jobmanager.impl.CellServiceConstraintObserver;
import seraph.zion.signal.jobmanager.impl.ChangeNumberConstraint;
import seraph.zion.signal.jobmanager.impl.ChangeNumberConstraintObserver;
import seraph.zion.signal.jobmanager.impl.ChargingConstraint;
import seraph.zion.signal.jobmanager.impl.ChargingConstraintObserver;
import seraph.zion.signal.jobmanager.impl.DataRestoreConstraint;
import seraph.zion.signal.jobmanager.impl.DataRestoreConstraintObserver;
import seraph.zion.signal.jobmanager.impl.DecryptionsDrainedConstraint;
import seraph.zion.signal.jobmanager.impl.DecryptionsDrainedConstraintObserver;
import seraph.zion.signal.jobmanager.impl.NetworkConstraint;
import seraph.zion.signal.jobmanager.impl.NetworkConstraintObserver;
import seraph.zion.signal.jobmanager.impl.NetworkOrCellServiceConstraint;
import seraph.zion.signal.jobmanager.impl.NotInCallConstraint;
import seraph.zion.signal.jobmanager.impl.NotInCallConstraintObserver;
import seraph.zion.signal.jobmanager.impl.SqlCipherMigrationConstraint;
import seraph.zion.signal.jobmanager.impl.SqlCipherMigrationConstraintObserver;
import seraph.zion.signal.jobmanager.migrations.DonationReceiptRedemptionJobMigration;
import seraph.zion.signal.jobmanager.migrations.PushDecryptMessageJobEnvelopeMigration;
import seraph.zion.signal.jobmanager.migrations.PushProcessMessageJobMigration;
import seraph.zion.signal.jobmanager.migrations.PushProcessMessageQueueJobMigration;
import seraph.zion.signal.jobmanager.migrations.RecipientIdFollowUpJobMigration;
import seraph.zion.signal.jobmanager.migrations.RecipientIdFollowUpJobMigration2;
import seraph.zion.signal.jobmanager.migrations.RecipientIdJobMigration;
import seraph.zion.signal.jobmanager.migrations.RetrieveProfileJobMigration;
import seraph.zion.signal.jobmanager.migrations.SendReadReceiptsJobMigration;
import seraph.zion.signal.jobmanager.migrations.SenderKeyDistributionSendJobRecipientMigration;
import seraph.zion.signal.migrations.AccountConsistencyMigrationJob;
import seraph.zion.signal.migrations.AccountRecordMigrationJob;
import seraph.zion.signal.migrations.ApplyUnknownFieldsToSelfMigrationJob;
import seraph.zion.signal.migrations.AttachmentCleanupMigrationJob;
import seraph.zion.signal.migrations.AttributesMigrationJob;
import seraph.zion.signal.migrations.AvatarIdRemovalMigrationJob;
import seraph.zion.signal.migrations.AvatarMigrationJob;
import seraph.zion.signal.migrations.BackupJitterMigrationJob;
import seraph.zion.signal.migrations.BackupNotificationMigrationJob;
import seraph.zion.signal.migrations.BlobStorageLocationMigrationJob;
import seraph.zion.signal.migrations.CachedAttachmentsMigrationJob;
import seraph.zion.signal.migrations.ClearGlideCacheMigrationJob;
import seraph.zion.signal.migrations.CopyUsernameToSignalStoreMigrationJob;
import seraph.zion.signal.migrations.DatabaseMigrationJob;
import seraph.zion.signal.migrations.DeleteDeprecatedLogsMigrationJob;
import seraph.zion.signal.migrations.DirectoryRefreshMigrationJob;
import seraph.zion.signal.migrations.EmojiDownloadMigrationJob;
import seraph.zion.signal.migrations.EmojiSearchIndexCheckMigrationJob;
import seraph.zion.signal.migrations.IdentityTableCleanupMigrationJob;
import seraph.zion.signal.migrations.LegacyMigrationJob;
import seraph.zion.signal.migrations.MigrationCompleteJob;
import seraph.zion.signal.migrations.OptimizeMessageSearchIndexMigrationJob;
import seraph.zion.signal.migrations.PassingMigrationJob;
import seraph.zion.signal.migrations.PinOptOutMigration;
import seraph.zion.signal.migrations.PinReminderMigrationJob;
import seraph.zion.signal.migrations.PniAccountInitializationMigrationJob;
import seraph.zion.signal.migrations.PniMigrationJob;
import seraph.zion.signal.migrations.PreKeysSyncMigrationJob;
import seraph.zion.signal.migrations.ProfileMigrationJob;
import seraph.zion.signal.migrations.ProfileSharingUpdateMigrationJob;
import seraph.zion.signal.migrations.RebuildMessageSearchIndexMigrationJob;
import seraph.zion.signal.migrations.RecheckPaymentsMigrationJob;
import seraph.zion.signal.migrations.RecipientSearchMigrationJob;
import seraph.zion.signal.migrations.SelfRegisteredStateMigrationJob;
import seraph.zion.signal.migrations.StickerAdditionMigrationJob;
import seraph.zion.signal.migrations.StickerDayByDayMigrationJob;
import seraph.zion.signal.migrations.StickerLaunchMigrationJob;
import seraph.zion.signal.migrations.StickerMyDailyLifeMigrationJob;
import seraph.zion.signal.migrations.StorageCapabilityMigrationJob;
import seraph.zion.signal.migrations.StorageFixLocalUnknownMigrationJob;
import seraph.zion.signal.migrations.StorageServiceMigrationJob;
import seraph.zion.signal.migrations.StorageServiceSystemNameMigrationJob;
import seraph.zion.signal.migrations.StoryViewedReceiptsStateMigrationJob;
import seraph.zion.signal.migrations.Svr2MirrorMigrationJob;
import seraph.zion.signal.migrations.SyncDistributionListsMigrationJob;
import seraph.zion.signal.migrations.SyncKeysMigrationJob;
import seraph.zion.signal.migrations.TrimByLengthSettingsMigrationJob;
import seraph.zion.signal.migrations.UpdateSmsJobsMigrationJob;
import seraph.zion.signal.migrations.UserNotificationMigrationJob;
import seraph.zion.signal.migrations.UuidMigrationJob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JobManagerFactories {

  public static Map<String, Job.Factory> getJobFactories(@NonNull Application application) {
    return new HashMap<String, Job.Factory>() {{
      put(AccountConsistencyWorkerJob.KEY,           new AccountConsistencyWorkerJob.Factory());
      put(AttachmentCompressionJob.KEY,              new AttachmentCompressionJob.Factory());
      put(AttachmentCopyJob.KEY,                     new AttachmentCopyJob.Factory());
      put(AttachmentDownloadJob.KEY,                 new AttachmentDownloadJob.Factory());
      put(AttachmentMarkUploadedJob.KEY,             new AttachmentMarkUploadedJob.Factory());
      put(AttachmentUploadJob.KEY,                   new AttachmentUploadJob.Factory());
      put(AutomaticSessionResetJob.KEY,              new AutomaticSessionResetJob.Factory());
      put(AvatarGroupsV1DownloadJob.KEY,             new AvatarGroupsV1DownloadJob.Factory());
      put(AvatarGroupsV2DownloadJob.KEY,             new AvatarGroupsV2DownloadJob.Factory());
      put(BoostReceiptRequestResponseJob.KEY,        new BoostReceiptRequestResponseJob.Factory());
      put(CallLinkPeekJob.KEY,                       new CallLinkPeekJob.Factory());
      put(CallLinkUpdateSendJob.KEY,                 new CallLinkUpdateSendJob.Factory());
      put(CallLogEventSendJob.KEY,                   new CallLogEventSendJob.Factory());
      put(CallSyncEventJob.KEY,                      new CallSyncEventJob.Factory());
      put(CheckServiceReachabilityJob.KEY,           new CheckServiceReachabilityJob.Factory());
      put(CleanPreKeysJob.KEY,                       new CleanPreKeysJob.Factory());
      put(ConversationShortcutRankingUpdateJob.KEY,  new ConversationShortcutRankingUpdateJob.Factory());
      put(ConversationShortcutUpdateJob.KEY,         new ConversationShortcutUpdateJob.Factory());
      put(CreateReleaseChannelJob.KEY,               new CreateReleaseChannelJob.Factory());
      put(DirectoryRefreshJob.KEY,                   new DirectoryRefreshJob.Factory());
      put(DonationReceiptRedemptionJob.KEY,          new DonationReceiptRedemptionJob.Factory());
      put(DownloadLatestEmojiDataJob.KEY,            new DownloadLatestEmojiDataJob.Factory());
      put(EmojiSearchIndexDownloadJob.KEY,           new EmojiSearchIndexDownloadJob.Factory());
      put(FcmRefreshJob.KEY,                         new FcmRefreshJob.Factory());
      put(FetchRemoteMegaphoneImageJob.KEY,          new FetchRemoteMegaphoneImageJob.Factory());
      put(FontDownloaderJob.KEY,                     new FontDownloaderJob.Factory());
      put(ForceUpdateGroupV2Job.KEY,                 new ForceUpdateGroupV2Job.Factory());
      put(ForceUpdateGroupV2WorkerJob.KEY,           new ForceUpdateGroupV2WorkerJob.Factory());
      put(GenerateAudioWaveFormJob.KEY,              new GenerateAudioWaveFormJob.Factory());
      put(GiftSendJob.KEY,                           new GiftSendJob.Factory());
      put(GroupCallUpdateSendJob.KEY,                new GroupCallUpdateSendJob.Factory());
      put(GroupCallPeekJob.KEY,                      new GroupCallPeekJob.Factory());
      put(GroupCallPeekWorkerJob.KEY,                new GroupCallPeekWorkerJob.Factory());
      put(GroupRingCleanupJob.KEY,                   new GroupRingCleanupJob.Factory());
      put(GroupV2UpdateSelfProfileKeyJob.KEY,        new GroupV2UpdateSelfProfileKeyJob.Factory());
      put(IndividualSendJob.KEY,                     new IndividualSendJob.Factory());
      put(LeaveGroupV2Job.KEY,                       new LeaveGroupV2Job.Factory());
      put(LeaveGroupV2WorkerJob.KEY,                 new LeaveGroupV2WorkerJob.Factory());
      put(LegacyAttachmentUploadJob.KEY,             new LegacyAttachmentUploadJob.Factory());
      put(LocalBackupJob.KEY,                        new LocalBackupJob.Factory());
      put(LocalBackupJobApi29.KEY,                   new LocalBackupJobApi29.Factory());
      put(MarkerJob.KEY,                             new MarkerJob.Factory());
      put(MmsSendJob.KEY,                            new MmsSendJob.Factory());
      put(MultiDeviceBlockedUpdateJob.KEY,           new MultiDeviceBlockedUpdateJob.Factory());
      put(MultiDeviceCallLinkSyncJob.KEY,            new MultiDeviceCallLinkSyncJob.Factory());
      put(MultiDeviceConfigurationUpdateJob.KEY,     new MultiDeviceConfigurationUpdateJob.Factory());
      put(MultiDeviceContactSyncJob.KEY,             new MultiDeviceContactSyncJob.Factory());
      put(MultiDeviceContactUpdateJob.KEY,           new MultiDeviceContactUpdateJob.Factory());
      put(MultiDeviceKeysUpdateJob.KEY,              new MultiDeviceKeysUpdateJob.Factory());
      put(MultiDeviceMessageRequestResponseJob.KEY,  new MultiDeviceMessageRequestResponseJob.Factory());
      put(MultiDeviceOutgoingPaymentSyncJob.KEY,     new MultiDeviceOutgoingPaymentSyncJob.Factory());
      put(MultiDeviceProfileContentUpdateJob.KEY,    new MultiDeviceProfileContentUpdateJob.Factory());
      put(MultiDeviceProfileKeyUpdateJob.KEY,        new MultiDeviceProfileKeyUpdateJob.Factory());
      put(MultiDeviceReadUpdateJob.KEY,              new MultiDeviceReadUpdateJob.Factory());
      put(MultiDeviceStickerPackOperationJob.KEY,    new MultiDeviceStickerPackOperationJob.Factory());
      put(MultiDeviceStickerPackSyncJob.KEY,         new MultiDeviceStickerPackSyncJob.Factory());
      put(MultiDeviceStorageSyncRequestJob.KEY,      new MultiDeviceStorageSyncRequestJob.Factory());
      put(MultiDeviceSubscriptionSyncRequestJob.KEY, new MultiDeviceSubscriptionSyncRequestJob.Factory());
      put(MultiDeviceVerifiedUpdateJob.KEY,          new MultiDeviceVerifiedUpdateJob.Factory());
      put(MultiDeviceViewOnceOpenJob.KEY,            new MultiDeviceViewOnceOpenJob.Factory());
      put(MultiDeviceViewedUpdateJob.KEY,            new MultiDeviceViewedUpdateJob.Factory());
      put(NullMessageSendJob.KEY,                    new NullMessageSendJob.Factory());
      put(OptimizeMessageSearchIndexJob.KEY,         new OptimizeMessageSearchIndexJob.Factory());
      put(PaymentLedgerUpdateJob.KEY,                new PaymentLedgerUpdateJob.Factory());
      put(PaymentNotificationSendJob.KEY,            new PaymentNotificationSendJob.Factory());
      put(PaymentNotificationSendJobV2.KEY,          new PaymentNotificationSendJobV2.Factory());
      put(PaymentSendJob.KEY,                        new PaymentSendJob.Factory());
      put(PaymentTransactionCheckJob.KEY,            new PaymentTransactionCheckJob.Factory());
      put(PnpInitializeDevicesJob.KEY,               new PnpInitializeDevicesJob.Factory());
      put(PreKeysSyncJob.KEY,                        new PreKeysSyncJob.Factory());
      put(ExternalLaunchDonationJob.KEY,             new ExternalLaunchDonationJob.Factory());
      put(ProfileKeySendJob.KEY,                     new ProfileKeySendJob.Factory());
      put(ProfileUploadJob.KEY,                      new ProfileUploadJob.Factory());
      put(PushDistributionListSendJob.KEY,           new PushDistributionListSendJob.Factory());
      put(PushGroupSendJob.KEY,                      new PushGroupSendJob.Factory());
      put(PushGroupSilentUpdateSendJob.KEY,          new PushGroupSilentUpdateSendJob.Factory());
      put(MessageFetchJob.KEY,                       new MessageFetchJob.Factory());
      put(PushProcessEarlyMessagesJob.KEY,           new PushProcessEarlyMessagesJob.Factory());
      put(PushProcessMessageErrorJob.KEY,            new PushProcessMessageErrorJob.Factory());
      put(PushProcessMessageJob.KEY,                 new PushProcessMessageJob.Factory());
      put(ReactionSendJob.KEY,                       new ReactionSendJob.Factory());
      put(RebuildMessageSearchIndexJob.KEY,          new RebuildMessageSearchIndexJob.Factory());
      put(ReclaimUsernameAndLinkJob.KEY,             new ReclaimUsernameAndLinkJob.Factory());
      put(RefreshAttributesJob.KEY,                  new RefreshAttributesJob.Factory());
      put(RefreshCallLinkDetailsJob.KEY,             new RefreshCallLinkDetailsJob.Factory());
      put(RefreshSvrCredentialsJob.KEY,              new RefreshSvrCredentialsJob.Factory());
      put(RefreshOwnProfileJob.KEY,                  new RefreshOwnProfileJob.Factory());
      put(RemoteConfigRefreshJob.KEY,                new RemoteConfigRefreshJob.Factory());
      put(RemoteDeleteSendJob.KEY,                   new RemoteDeleteSendJob.Factory());
      put(ReportSpamJob.KEY,                         new ReportSpamJob.Factory());
      put(ResendMessageJob.KEY,                      new ResendMessageJob.Factory());
      put(ResumableUploadSpecJob.KEY,                new ResumableUploadSpecJob.Factory());
      put(RequestGroupV2InfoWorkerJob.KEY,           new RequestGroupV2InfoWorkerJob.Factory());
      put(RequestGroupV2InfoJob.KEY,                 new RequestGroupV2InfoJob.Factory());
      put(RetrieveProfileAvatarJob.KEY,              new RetrieveProfileAvatarJob.Factory());
      put(RetrieveProfileJob.KEY,                    new RetrieveProfileJob.Factory());
      put(RetrieveRemoteAnnouncementsJob.KEY,        new RetrieveRemoteAnnouncementsJob.Factory());
      put(RotateCertificateJob.KEY,                  new RotateCertificateJob.Factory());
      put(RotateProfileKeyJob.KEY,                   new RotateProfileKeyJob.Factory());
      put(SenderKeyDistributionSendJob.KEY,          new SenderKeyDistributionSendJob.Factory());
      put(SendDeliveryReceiptJob.KEY,                new SendDeliveryReceiptJob.Factory());
      put(SendPaymentsActivatedJob.KEY,              new SendPaymentsActivatedJob.Factory());
      put(SendReadReceiptJob.KEY,                    new SendReadReceiptJob.Factory(application));
      put(SendRetryReceiptJob.KEY,                   new SendRetryReceiptJob.Factory());
      put(SendViewedReceiptJob.KEY,                  new SendViewedReceiptJob.Factory(application));
      put(SyncSystemContactLinksJob.KEY,             new SyncSystemContactLinksJob.Factory());
      put(MultiDeviceStorySendSyncJob.KEY,           new MultiDeviceStorySendSyncJob.Factory());
      put(ResetSvrGuessCountJob.KEY,                 new ResetSvrGuessCountJob.Factory());
      put(ServiceOutageDetectionJob.KEY,             new ServiceOutageDetectionJob.Factory());
      put(SmsSendJob.KEY,                            new SmsSendJob.Factory());
      put(SmsSentJob.KEY,                            new SmsSentJob.Factory());
      put(StickerDownloadJob.KEY,                    new StickerDownloadJob.Factory());
      put(StickerPackDownloadJob.KEY,                new StickerPackDownloadJob.Factory());
      put(StorageAccountRestoreJob.KEY,              new StorageAccountRestoreJob.Factory());
      put(StorageForcePushJob.KEY,                   new StorageForcePushJob.Factory());
      put(StorageSyncJob.KEY,                        new StorageSyncJob.Factory());
      put(SubscriptionKeepAliveJob.KEY,              new SubscriptionKeepAliveJob.Factory());
      put(SubscriptionReceiptRequestResponseJob.KEY, new SubscriptionReceiptRequestResponseJob.Factory());
      put(StoryOnboardingDownloadJob.KEY,            new StoryOnboardingDownloadJob.Factory());
      put(SubmitRateLimitPushChallengeJob.KEY,       new SubmitRateLimitPushChallengeJob.Factory());
      put(Svr2MirrorJob.KEY,                         new Svr2MirrorJob.Factory());
      put(ThreadUpdateJob.KEY,                       new ThreadUpdateJob.Factory());
      put(TrimThreadJob.KEY,                         new TrimThreadJob.Factory());
      put(TypingSendJob.KEY,                         new TypingSendJob.Factory());
      put(ApkUpdateJob.KEY,                          new ApkUpdateJob.Factory());

      // Migrations
      put(AccountConsistencyMigrationJob.KEY,        new AccountConsistencyMigrationJob.Factory());
      put(AccountRecordMigrationJob.KEY,             new AccountRecordMigrationJob.Factory());
      put(ApplyUnknownFieldsToSelfMigrationJob.KEY,  new ApplyUnknownFieldsToSelfMigrationJob.Factory());
      put(AttachmentCleanupMigrationJob.KEY,         new AttachmentCleanupMigrationJob.Factory());
      put(AttributesMigrationJob.KEY,                new AttributesMigrationJob.Factory());
      put(AvatarIdRemovalMigrationJob.KEY,           new AvatarIdRemovalMigrationJob.Factory());
      put(AvatarMigrationJob.KEY,                    new AvatarMigrationJob.Factory());
      put(BackupJitterMigrationJob.KEY,              new BackupJitterMigrationJob.Factory());
      put(BackupNotificationMigrationJob.KEY,        new BackupNotificationMigrationJob.Factory());
      put(BlobStorageLocationMigrationJob.KEY,       new BlobStorageLocationMigrationJob.Factory());
      put(CachedAttachmentsMigrationJob.KEY,         new CachedAttachmentsMigrationJob.Factory());
      put(ClearGlideCacheMigrationJob.KEY,           new ClearGlideCacheMigrationJob.Factory());
      put(CopyUsernameToSignalStoreMigrationJob.KEY, new CopyUsernameToSignalStoreMigrationJob.Factory());
      put(DatabaseMigrationJob.KEY,                  new DatabaseMigrationJob.Factory());
      put(DeleteDeprecatedLogsMigrationJob.KEY,      new DeleteDeprecatedLogsMigrationJob.Factory());
      put(DirectoryRefreshMigrationJob.KEY,          new DirectoryRefreshMigrationJob.Factory());
      put(EmojiDownloadMigrationJob.KEY,             new EmojiDownloadMigrationJob.Factory());
      put(EmojiSearchIndexCheckMigrationJob.KEY,     new EmojiSearchIndexCheckMigrationJob.Factory());
      put(IdentityTableCleanupMigrationJob.KEY,      new IdentityTableCleanupMigrationJob.Factory());
      put(LegacyMigrationJob.KEY,                    new LegacyMigrationJob.Factory());
      put(MigrationCompleteJob.KEY,                  new MigrationCompleteJob.Factory());
      put(OptimizeMessageSearchIndexMigrationJob.KEY,new OptimizeMessageSearchIndexMigrationJob.Factory());
      put(PinOptOutMigration.KEY,                    new PinOptOutMigration.Factory());
      put(PinReminderMigrationJob.KEY,               new PinReminderMigrationJob.Factory());
      put(PniAccountInitializationMigrationJob.KEY,  new PniAccountInitializationMigrationJob.Factory());
      put(PniMigrationJob.KEY,                       new PniMigrationJob.Factory());
      put(PreKeysSyncMigrationJob.KEY,               new PreKeysSyncMigrationJob.Factory());
      put(ProfileMigrationJob.KEY,                   new ProfileMigrationJob.Factory());
      put(ProfileSharingUpdateMigrationJob.KEY,      new ProfileSharingUpdateMigrationJob.Factory());
      put(RebuildMessageSearchIndexMigrationJob.KEY, new RebuildMessageSearchIndexMigrationJob.Factory());
      put(RecheckPaymentsMigrationJob.KEY,           new RecheckPaymentsMigrationJob.Factory());
      put(RecipientSearchMigrationJob.KEY,           new RecipientSearchMigrationJob.Factory());
      put(SelfRegisteredStateMigrationJob.KEY,       new SelfRegisteredStateMigrationJob.Factory());
      put(StickerLaunchMigrationJob.KEY,             new StickerLaunchMigrationJob.Factory());
      put(StickerAdditionMigrationJob.KEY,           new StickerAdditionMigrationJob.Factory());
      put(StickerDayByDayMigrationJob.KEY,           new StickerDayByDayMigrationJob.Factory());
      put(StickerMyDailyLifeMigrationJob.KEY,        new StickerMyDailyLifeMigrationJob.Factory());
      put(StorageCapabilityMigrationJob.KEY,         new StorageCapabilityMigrationJob.Factory());
      put(StorageFixLocalUnknownMigrationJob.KEY,    new StorageFixLocalUnknownMigrationJob.Factory());
      put(StorageServiceMigrationJob.KEY,            new StorageServiceMigrationJob.Factory());
      put(StorageServiceSystemNameMigrationJob.KEY,  new StorageServiceSystemNameMigrationJob.Factory());
      put(StoryViewedReceiptsStateMigrationJob.KEY,  new StoryViewedReceiptsStateMigrationJob.Factory());
      put(Svr2MirrorMigrationJob.KEY,                new Svr2MirrorMigrationJob.Factory());
      put(SyncDistributionListsMigrationJob.KEY,     new SyncDistributionListsMigrationJob.Factory());
      put(SyncKeysMigrationJob.KEY,                  new SyncKeysMigrationJob.Factory());
      put(TrimByLengthSettingsMigrationJob.KEY,      new TrimByLengthSettingsMigrationJob.Factory());
      put(UpdateSmsJobsMigrationJob.KEY,             new UpdateSmsJobsMigrationJob.Factory());
      put(UserNotificationMigrationJob.KEY,          new UserNotificationMigrationJob.Factory());
      put(UuidMigrationJob.KEY,                      new UuidMigrationJob.Factory());

      // Dead jobs
      put(FailingJob.KEY,                            new FailingJob.Factory());
      put(PassingMigrationJob.KEY,                   new PassingMigrationJob.Factory());
      put("PushContentReceiveJob",                   new FailingJob.Factory());
      put("AttachmentUploadJob",                     new FailingJob.Factory());
      put("MmsSendJob",                              new FailingJob.Factory());
      put("RefreshUnidentifiedDeliveryAbilityJob",   new FailingJob.Factory());
      put("Argon2TestJob",                           new FailingJob.Factory());
      put("Argon2TestMigrationJob",                  new PassingMigrationJob.Factory());
      put("StorageKeyRotationMigrationJob",          new PassingMigrationJob.Factory());
      put("StorageSyncJob",                          new StorageSyncJob.Factory());
      put("WakeGroupV2Job",                          new FailingJob.Factory());
      put("LeaveGroupJob",                           new FailingJob.Factory());
      put("PushGroupUpdateJob",                      new FailingJob.Factory());
      put("RequestGroupInfoJob",                     new FailingJob.Factory());
      put("RotateSignedPreKeyJob",                   new PreKeysSyncJob.Factory());
      put("CreateSignedPreKeyJob",                   new PreKeysSyncJob.Factory());
      put("RefreshPreKeysJob",                       new PreKeysSyncJob.Factory());
      put("RecipientChangedNumberJob",               new FailingJob.Factory());
      put("PushTextSendJob",                         new IndividualSendJob.Factory());
      put("MultiDevicePniIdentityUpdateJob",         new FailingJob.Factory());
      put("MultiDeviceGroupUpdateJob",               new FailingJob.Factory());
      put("CallSyncEventJob",                        new FailingJob.Factory());
      put("RegistrationPinV2MigrationJob",           new FailingJob.Factory());
      put("KbsEnclaveMigrationWorkerJob",            new FailingJob.Factory());
      put("KbsEnclaveMigrationJob",                  new PassingMigrationJob.Factory());
      put("ClearFallbackKbsEnclaveJob",              new FailingJob.Factory());
      put("PushDecryptJob",                          new FailingJob.Factory());
      put("PushDecryptDrainedJob",                   new FailingJob.Factory());
      put("PushProcessJob",                          new FailingJob.Factory());
      put("DecryptionsDrainedMigrationJob",          new PassingMigrationJob.Factory());
      put("MmsReceiveJob",                           new FailingJob.Factory());
      put("MmsDownloadJob",                          new FailingJob.Factory());
      put("SmsReceiveJob",                           new FailingJob.Factory());
      put("StoryReadStateMigrationJob",              new PassingMigrationJob.Factory());
      put("GroupV1MigrationJob",                     new FailingJob.Factory());
      put("NewRegistrationUsernameSyncJob",          new FailingJob.Factory());
    }};
  }

  public static Map<String, Constraint.Factory> getConstraintFactories(@NonNull Application application) {
    return new HashMap<String, Constraint.Factory>() {{
      put(AutoDownloadEmojiConstraint.KEY,           new AutoDownloadEmojiConstraint.Factory(application));
      put(ChangeNumberConstraint.KEY,                new ChangeNumberConstraint.Factory());
      put(ChargingConstraint.KEY,                    new ChargingConstraint.Factory());
      put(DataRestoreConstraint.KEY,                 new DataRestoreConstraint.Factory());
      put(DecryptionsDrainedConstraint.KEY,          new DecryptionsDrainedConstraint.Factory());
      put(NetworkConstraint.KEY,                     new NetworkConstraint.Factory(application));
      put(NetworkOrCellServiceConstraint.KEY,        new NetworkOrCellServiceConstraint.Factory(application));
      put(NetworkOrCellServiceConstraint.LEGACY_KEY, new NetworkOrCellServiceConstraint.Factory(application));
      put(NotInCallConstraint.KEY,                   new NotInCallConstraint.Factory());
      put(SqlCipherMigrationConstraint.KEY,          new SqlCipherMigrationConstraint.Factory(application));
    }};
  }

  public static List<ConstraintObserver> getConstraintObservers(@NonNull Application application) {
    return Arrays.asList(CellServiceConstraintObserver.getInstance(application),
                         new ChargingConstraintObserver(application),
                         new NetworkConstraintObserver(application),
                         new SqlCipherMigrationConstraintObserver(),
                         new DecryptionsDrainedConstraintObserver(),
                         new NotInCallConstraintObserver(),
                         ChangeNumberConstraintObserver.INSTANCE,
                         DataRestoreConstraintObserver.INSTANCE);
  }

  public static List<JobMigration> getJobMigrations(@NonNull Application application) {
    return Arrays.asList(new RecipientIdJobMigration(application),
                         new RecipientIdFollowUpJobMigration(),
                         new RecipientIdFollowUpJobMigration2(),
                         new SendReadReceiptsJobMigration(SignalDatabase.messages()),
                         new PushProcessMessageQueueJobMigration(application),
                         new RetrieveProfileJobMigration(),
                         new PushDecryptMessageJobEnvelopeMigration(),
                         new SenderKeyDistributionSendJobRecipientMigration(),
                         new PushProcessMessageJobMigration(),
                         new DonationReceiptRedemptionJobMigration());
  }
}

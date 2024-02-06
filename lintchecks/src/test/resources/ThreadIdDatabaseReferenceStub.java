package seraph.zion.signal.database;

interface ThreadIdDatabaseReference {
  void remapThread(long fromId, long toId);
}

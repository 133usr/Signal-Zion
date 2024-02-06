package seraph.zion.signal.conversation.colors

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.annimon.stream.Stream
import org.signal.core.util.MapUtil
import seraph.zion.signal.conversation.colors.ChatColorsPalette.Names.all
import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.groups.LiveGroup
import seraph.zion.signal.groups.ui.GroupMemberEntry.FullMember
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.util.DefaultValueLiveData
import java.util.Optional

object NameColors {

  fun createSessionMembersCache(): MutableMap<GroupId, Set<Recipient>> {
    return mutableMapOf()
  }

  fun getNameColorsMapLiveData(
    recipientId: LiveData<RecipientId>,
    sessionMemberCache: MutableMap<GroupId, Set<Recipient>>
  ): LiveData<Map<RecipientId, NameColor>> {
    val recipient = recipientId.switchMap { r: RecipientId? -> Recipient.live(r!!).liveData }
    val group = recipient.map { obj: Recipient -> obj.groupId }
    val groupMembers = group.switchMap { g: Optional<GroupId> ->
      g.map { groupId: GroupId -> this.getSessionGroupRecipients(groupId, sessionMemberCache) }
        .orElseGet { DefaultValueLiveData(emptySet()) }
    }
    return groupMembers.map { members: Set<Recipient>? ->
      val sorted = Stream.of(members)
        .filter { member: Recipient? -> member != Recipient.self() }
        .sortBy { obj: Recipient -> obj.requireStringId() }
        .toList()
      val names = all
      val colors: MutableMap<RecipientId, NameColor> = HashMap()
      for (i in sorted.indices) {
        colors[sorted[i].id] = names[i % names.size]
      }
      colors
    }
  }

  private fun getSessionGroupRecipients(groupId: GroupId, sessionMemberCache: MutableMap<GroupId, Set<Recipient>>): LiveData<Set<Recipient>> {
    val fullMembers = LiveGroup(groupId)
      .fullMembers
      .map { members: List<FullMember>? ->
        Stream.of(members)
          .map { it.member }
          .toList()
      }

    return fullMembers.map { currentMembership: List<Recipient>? ->
      val cachedMembers: MutableSet<Recipient> = MapUtil.getOrDefault(sessionMemberCache, groupId, HashSet()).toMutableSet()
      cachedMembers.addAll(currentMembership!!)
      sessionMemberCache[groupId] = cachedMembers
      cachedMembers
    }
  }
}

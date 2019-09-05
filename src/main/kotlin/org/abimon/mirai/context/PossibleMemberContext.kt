package org.abimon.mirai.context

import java.time.Instant

interface PossibleMemberContext: UserContext {
    val nickname: String?
    val roles: Array<PendingRoleContext>?
    val joinedAt: Instant?
}
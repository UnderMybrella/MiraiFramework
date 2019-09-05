package org.abimon.mirai.context

import java.time.Instant

interface MemberContext: PossibleMemberContext {
    override val roles: Array<PendingRoleContext>
    override val joinedAt: Instant
}
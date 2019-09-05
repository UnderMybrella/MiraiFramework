package org.abimon.mirai.context

import java.time.Instant

data class DelegatedUserContext(val delegate: UserContext) : PossibleMemberContext, UserContext by delegate {
    override val joinedAt: Instant? = null
    override val nickname: String? = null
    override val roles: Array<PendingRoleContext>? = null
}
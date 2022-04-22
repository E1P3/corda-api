@file:JvmName("MemberInfoHelpers")
package net.corda.v5.membership

import net.corda.data.KeyValuePair
import net.corda.data.KeyValuePairList

fun MemberInfo.toAvro() = net.corda.data.membership.MemberInfo(
    KeyValuePairList(
        memberProvidedContext.entries.map {
            KeyValuePair(it.key, it.value)
        }
    ),
    KeyValuePairList(
        mgmProvidedContext.entries.map {
            KeyValuePair(it.key, it.value)
        }
    )
)

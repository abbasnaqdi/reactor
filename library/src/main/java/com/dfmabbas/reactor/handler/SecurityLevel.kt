package com.dfmabbas.reactor.handler


/*
* The algorithms of this enum are:
* POWERFUL : AES
* FAST : Base64
* NONE : Not encrypted
*/

enum class SecurityLevel {
    POWERFUL,
    FAST,
    NONE
}
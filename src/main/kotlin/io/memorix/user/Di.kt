package io.memorix.user

import org.koin.dsl.module

val userDi = module {
    single { UserRepository(get()) }
}


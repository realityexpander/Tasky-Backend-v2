package com.realityexpander.util

import com.realityexpander.di.testAppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
abstract class RootTest: KoinTest {

    protected val dispatchers by inject<TestDispatcherProvider>()
    protected val scheduler by lazy { dispatchers.dispatcher.scheduler }

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(testAppModule)
        }

        Dispatchers.setMain(dispatchers.main)
        runBlocking {
            setupFakeDataSources()
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        cleanUpDataSources()
        stopKoin()
    }
}
package com.example.simplecleanarchitecture.core.lib.utils

import com.example.simplecleanarchitecture.core.lib.viewmodel.BaseUiStateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.spy

class FlowObserver<T> {

    private var job: Job? = null

    fun onError(@Suppress("UNUSED_PARAMETER") throwable: Throwable) {}

    fun onEach(@Suppress("UNUSED_PARAMETER") item: T) {}

    fun onCompletion() {}

    fun setup(job: Job) {
        this.job = job
    }

    fun cancel() {
        job!!.cancel()
    }
}

suspend fun <T> Flow<T>.mockObserver(
    coroutineScope: CoroutineScope,
    clearMocks: Boolean = false,
    onEachCallback: (T) -> Unit = {},
    onErrorCallback: (Throwable) -> Unit = {},
    onCompletion: () -> Unit = {}
): FlowObserver<T> {
    val observerMock: FlowObserver<T> = spy(FlowObserver())
    val job = coroutineScope.launch {
        this@mockObserver
            .catch {
                observerMock.onError(it)
                onErrorCallback.invoke(it)
            }
            .onEach {
                observerMock.onEach(it)
                onEachCallback.invoke(it)
            }
            .onCompletion {
                observerMock.onCompletion()
                onCompletion.invoke()
            }
            .collect()
    }
    observerMock.setup(job)
    if (clearMocks) {
        clearInvocations(observerMock)
    }
    return observerMock
}

suspend fun <UiState : Any, UiEffect : Any> TestScope.observeViewModel(
    viewModel: BaseUiStateViewModel<UiState, UiEffect>,
    callback: (FlowObserver<UiState>, FlowObserver<UiEffect>) -> Unit
) {
    val stateObserver = viewModel.uiState.mockObserver(this, true)
    val effectObserver = viewModel.uiEffect.mockObserver(this, true)
    callback.invoke(stateObserver, effectObserver)
    effectObserver.cancel()
    stateObserver.cancel()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <UiState : Any, UiEffect : Any> runViewModelTest(
    viewModel: BaseUiStateViewModel<UiState, UiEffect>,
    callback: (FlowObserver<UiState>, FlowObserver<UiEffect>) -> Unit
) = runTest(UnconfinedTestDispatcher()) {
    observeViewModel(viewModel, callback)
}

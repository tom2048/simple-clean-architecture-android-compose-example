package com.example.simplecleanarchitecture.users.ui.passwordchange

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.users.usecase.user.UserPasswordUpdateUseCase
import com.github.terrakok.cicerone.Back
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserPasswordChangeViewModelTest : TestHelper by DefaultTestHelper() {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserPasswordChangeViewModel
    private lateinit var appResources: AppResources
    private lateinit var passwordUpdateUseCase: UserPasswordUpdateUseCase

    @BeforeTest
    fun setupTest() {
        prepareLifecycle()

        appResources = mock {
            on { getStringResource(R.string.common_communication_error) } doReturn "Error during processing your request."
            on { getStringResource(R.string.password_validation_message) } doReturn "Password is incorrect"
            on { getStringResource(R.string.password_confirmation_validation_message) } doReturn "Passwords are not equal"
        }
        passwordUpdateUseCase = mock {
            on { invoke(any(), eq(VALID_PASSWORD)) } doReturn testFlowOf(Unit)
            on { invoke(any(), eq(INVALID_PASSWORD)) } doReturn testFlowOf(TestException("Invalid password"))
        }
        //appResources = mock<AppResources>().apply {
        //    whenever(getStringResource(R.string.common_communication_error)).thenReturn("Error during processing your request.")
        //    whenever(getStringResource(R.string.password_validation_message)).thenReturn("Password is incorrect")
        //    whenever(getStringResource(R.string.password_confirmation_validation_message)).thenReturn("Passwords are not equal")
        //}
        //passwordUpdateUseCase = mock<UserPasswordUpdateUseCase>().apply {
        //    whenever(invoke(any(), eq(VALID_PASSWORD))).thenReturn(testFlowOf(Unit))
        //    whenever(invoke(any(), eq(INVALID_PASSWORD))).thenReturn(testFlowOf(TestException("Invalid password")))
        //}
        viewModel = UserPasswordChangeViewModel(
            DEFAULT_USER_ID,
            SavedStateHandle(),
            passwordUpdateUseCase,
            appResources
        )
    }

    @AfterTest
    fun cleanupTest() {
        cleanUpLifecycle()
        invokeViewModelOnCleared(viewModel)
    }

    @Test
    fun `setPassword() should hide validation message when password is valid`() = runTest(UnconfinedTestDispatcher()) {
        val stateObserver = viewModel.uiState.mockObserver(this, true)

        viewModel.setPassword(VALID_PASSWORD)

        verify(stateObserver, only()).onEach(argWhere { it.password == VALID_PASSWORD && it.passwordValidation == "" })
        stateObserver.cancel()
    }

    @Test
    fun `setPassword() should show validation message when password is invalid`() = runTest(UnconfinedTestDispatcher()) {
        val stateObserver = viewModel.uiState.mockObserver(this, true)

        viewModel.setPassword(INVALID_PASSWORD)

        verify(stateObserver, only()).onEach(argWhere { it.password == INVALID_PASSWORD && it.passwordValidation.isNotEmpty() })
        stateObserver.cancel()
    }

    @Test
    fun `setPassword() should show validation message when confirmPassword differs`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPasswordConfirmed(VALID_PASSWORD)
            val stateObserver = viewModel.uiState.mockObserver(this, true)

            viewModel.setPassword(VALID_PASSWORD + "test")

            verify(stateObserver, only()).onEach(argWhere { it.passwordConfirmedValidation.isNotEmpty() })

            stateObserver.cancel()
        }

    @Test
    fun `setPassword() should hide validation message when confirmPassword equals`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPasswordConfirmed(VALID_PASSWORD)
            val stateObserver = viewModel.uiState.mockObserver(this, true)

            viewModel.setPassword(VALID_PASSWORD)

            verify(stateObserver, only()).onEach(argWhere { it.passwordConfirmedValidation.isEmpty() })

            stateObserver.cancel()
        }


    @Test
    fun `setPasswordConfirmed() should show validation message when password differs`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            val stateObserver = viewModel.uiState.mockObserver(this, true)

            viewModel.setPasswordConfirmed(VALID_PASSWORD + "test")

            verify(stateObserver, only()).onEach(argWhere { it.passwordConfirmedValidation.isNotEmpty() })

            stateObserver.cancel()
        }


    @Test
    fun `setPasswordConfirmed() should hide validation message when confirmPassword equals`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            val stateObserver = viewModel.uiState.mockObserver(this, true)

            viewModel.setPasswordConfirmed(VALID_PASSWORD)

            verify(stateObserver, only()).onEach(argWhere { it.passwordConfirmedValidation.isEmpty() })
            stateObserver.cancel()
        }


    @Test
    fun `setPassword() should enable submit button when both password are correct`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPasswordConfirmed(VALID_PASSWORD)
            val stateObserver = viewModel.uiState.mockObserver(this, true)

            viewModel.setPassword(VALID_PASSWORD)

            verify(stateObserver, only()).onEach(argWhere { it.isSubmitEnabled })

            stateObserver.cancel()
        }


    @Test
    fun `setPassword() should disable submit button when password or password confirmed is not correct`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            viewModel.setPasswordConfirmed(VALID_PASSWORD)
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.setPassword(INVALID_PASSWORD)

            verify(uiStateObserver).onEach(argWhere { !it.isSubmitEnabled })
            uiStateObserver.cancel()
        }


    @Test
    fun `setPasswordConfirmed() should enable submit button when both passwords are correct`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.setPasswordConfirmed(VALID_PASSWORD)

            verify(uiStateObserver).onEach(argWhere { it.isSubmitEnabled })

            uiStateObserver.cancel()
        }


    @Test
    fun `setPasswordConfirmed() should disable submit button when password or password confirm is invalid`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            viewModel.setPasswordConfirmed(VALID_PASSWORD)
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.setPasswordConfirmed(INVALID_PASSWORD)

            verify(uiStateObserver).onEach(argWhere { !it.isSubmitEnabled })

            uiStateObserver.cancel()
        }


    @Test
    fun `submit() should show and then not hide the preloader when password successfully changed`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            whenever(passwordUpdateUseCase.invoke(any(), any())).thenReturn(testFlowOf(Unit))

            viewModel.submit()

            verify(uiStateObserver).onEach(argWhere { it.preloader })

            uiStateObserver.cancel()
        }


    @Test
    fun `submit() should show and then hide the preloader when there were errors during password change`() =
    // Using runBlockingTest due to this issue: https://github.com/Kotlin/kotlinx.coroutines/issues/3367
        // TODO: when fixed, change to runTest(UnconfinedTestDispatcher()) {
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this, true)

            whenever(passwordUpdateUseCase.invoke(any(), any())).doReturn(testFlowOf(TestException()))

            viewModel.submit()

            inOrder(uiStateObserver) {
                verify(uiStateObserver).onEach(argWhere { it.preloader })
                verify(uiStateObserver).onEach(argWhere { !it.preloader })
            }

            uiStateObserver.cancel()
        }


    @Test
    fun `submit() should execute password update when password is valid`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(VALID_PASSWORD)
            whenever(passwordUpdateUseCase.invoke(DEFAULT_USER_ID, VALID_PASSWORD)).thenReturn(testFlowOf(Unit))

            viewModel.submit()

            verify(passwordUpdateUseCase).invoke(DEFAULT_USER_ID, VALID_PASSWORD)
        }


    @Test
    fun `submit() should show error message when there were some errors`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiEffectObserver = viewModel.uiEffect.mockObserver(this)
            viewModel.setPassword(INVALID_PASSWORD)
            whenever(passwordUpdateUseCase.invoke(any(), any())).thenReturn(testFlowOf(TestException()))

            viewModel.submit()

            verify(uiEffectObserver).onEach(argWhere { it is UserPasswordChangeViewModel.UiEffect.Message && it.text.isNotEmpty() })

            uiEffectObserver.cancel()
        }


    @Test
    fun `submit() should close the screen when there were no errors`() =
        runTest(UnconfinedTestDispatcher()) {
            viewModel.setPassword(INVALID_PASSWORD)
            val uiEffectObserver = viewModel.uiEffect.mockObserver(this, true)
            whenever(passwordUpdateUseCase.invoke(any(), any())).thenReturn(testFlowOf(Unit))

            viewModel.submit()

            verify(uiEffectObserver, only()).onEach(argWhere { it is UserPasswordChangeViewModel.UiEffect.Routing && it.command is Back })

            uiEffectObserver.cancel()
        }


    companion object {

        private const val VALID_PASSWORD = "V@lid001"
        private const val INVALID_PASSWORD = "short"
        private const val DEFAULT_USER_ID = "a312b3ee-84c2-11eb-8dcd-0242ac130003"

    }

}
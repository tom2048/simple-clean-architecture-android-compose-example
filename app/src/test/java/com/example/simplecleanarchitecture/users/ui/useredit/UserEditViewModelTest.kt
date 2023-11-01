package com.example.simplecleanarchitecture.users.ui.useredit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.extensions.last
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Message
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Routing
import com.example.simplecleanarchitecture.users.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase
import com.github.terrakok.cicerone.Back
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class UserEditViewModelTest : TestHelper by DefaultTestHelper() {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserEditViewModel

    private lateinit var userShowDetailsUseCase: UserShowDetailsUseCase
    private lateinit var userUpdateUseCase: UserUpdateUseCase
    private lateinit var appResources: AppResources

    @BeforeTest
    fun setup() {
        prepareLifecycle()

        userShowDetailsUseCase = mock()
        whenever(userShowDetailsUseCase.invoke(any())).thenReturn(testFlowOf(DEFAULT_USER))
        userUpdateUseCase = mock()
        appResources = mock()
        whenever(appResources.getStringResource(R.string.nickname_validation_message)).thenReturn("Validation error")
        whenever(appResources.getStringResource(R.string.email_validation_message)).thenReturn("Validation error")
        whenever(appResources.getStringResource(R.string.description_validation_message)).thenReturn("Validation error")
        whenever(appResources.getStringResource(R.string.common_communication_error)).thenReturn("Edit user")
        whenever(appResources.getStringResource(R.string.user_edit_header)).thenReturn("Edit user")
        whenever(appResources.getStringResource(R.string.user_add_header)).thenReturn("Add user")

        viewModel = UserEditViewModel(
            DEFAULT_USER.id!!,
            SavedStateHandle(),
            userShowDetailsUseCase,
            userUpdateUseCase,
            appResources
        )
    }

    @AfterTest
    fun cleanup() {
        cleanUpLifecycle()
        invokeViewModelOnCleared(viewModel)
    }


    @Test
    fun `submit() closes the form when there are no errors during save`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf())
            viewModel.loadDetails()
            val observer = viewModel.uiEffect.mockObserver(this)

            viewModel.submit()

            verify(observer, last()).onEach(argWhere { it is Routing && it.command is Back })
            observer.cancel()
        }

    @Test
    fun `submit() doesn't close the form when there is an error during save`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiEffectObserver = viewModel.uiEffect.mockObserver(this)
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf(ValidationException(listOf(Pair("test", "test")))))

            viewModel.submit()

            verify(uiEffectObserver, never()).onEach(argWhere { it is Routing })
            uiEffectObserver.cancel()
        }

    @Test
    fun `submit() displays an error message when there is an error during save`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf(ValidationException(listOf(Pair("test", "test")))))
            viewModel.loadDetails()
            val uiEffectObserver = viewModel.uiEffect.mockObserver(this, true)

            viewModel.submit()

            verify(uiEffectObserver, only()).onEach(argWhere { it is Message && it.text.isNotEmpty() })
            uiEffectObserver.cancel()
        }

    @Test
    fun `submit() invokes update use case when there are correct user details set`() =
        runTest(UnconfinedTestDispatcher()) {
            val observer = userUpdateUseCase(DEFAULT_USER_INPUT).mockObserver(this)
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf())
            viewModel.setEmail(DEFAULT_USER_INPUT.email)
            viewModel.setNickname(DEFAULT_USER_INPUT.nickname)

            viewModel.submit()

            verify(observer).onCompletion()
            verify(userUpdateUseCase, only()).invoke(any())

            /*coEvery { userUpdateUseCase.invoke(any()) } returns flow { emit(Unit) }
            val testObserver = userUpdateUseCase(DEFAULT_USER_INPUT).mockkTest()
            viewModel.setEmail(DEFAULT_USER_INPUT.email)
            viewModel.setNickname(DEFAULT_USER_INPUT.nickname)

            viewModel.submit()

            verify { testObserver.onCompletion() }
            coVerify { userUpdateUseCase.invoke(any()) }*/
        }

    @Test
    fun `setting nickname shows validation message when the nickname is invalid`() =
        runTest(UnconfinedTestDispatcher()) {
            val observer = viewModel.uiState.mockObserver(this)
            viewModel.setNickname(INVALID_NICKNAME)

            verify(observer, last()).onEach(argWhere { it.nicknameValidationError.isNotEmpty() })
            observer.cancel()
        }

    @Test
    fun `setting email shows validation message when the email is invalid`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            viewModel.setEmail(INVALID_EMAIL)

            verify(uiStateObserver, last()).onEach(argWhere { it.emailValidationError.isNotEmpty() })
            uiStateObserver.cancel()
        }

    @Test
    fun `setting description shows validation error message when the description is invalid`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            viewModel.setDescription(INVALID_DESCRIPTION)

            verify(uiStateObserver, last()).onEach(argWhere { it.descriptionValidationError.isNotEmpty() })
            uiStateObserver.cancel()
        }

    @Test
    fun `setting all values enables the submit button when values are valid`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.setNickname(VALID_NICKNAME)
            viewModel.setEmail(VALID_EMAIL)
            viewModel.setDescription(VALID_DESCRIPTION)

            inOrder(uiStateObserver) {
                uiStateObserver.onEach(argWhere { !it.isSubmitEnabled })
                uiStateObserver.onEach(argWhere { !it.isSubmitEnabled })
                uiStateObserver.onEach(argWhere { it.isSubmitEnabled })
            }
            uiStateObserver.cancel()
        }

    @Test
    fun `loadDetails() loads user data when the data is properly loaded`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(userShowDetailsUseCase.invoke(any())).thenReturn(testFlowOf(DEFAULT_USER))
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.loadDetails()

            verify(uiStateObserver, last()).onEach(argWhere {
                it.nickname == DEFAULT_USER.nickname &&
                        it.email == DEFAULT_USER.email &&
                        it.description == DEFAULT_USER.description
            })
            uiStateObserver.cancel()
        }

    @Test
    fun `loadDetails() shows and then hides the preloader when the data is properly loaded`() =
        runTest(UnconfinedTestDispatcher()) {

            val uiStateObserver = viewModel.uiState.mockObserver(this)
            whenever(userShowDetailsUseCase.invoke(any())).thenReturn(testFlowOf(DEFAULT_USER))

            viewModel.loadDetails()

            inOrder(uiStateObserver) {
                uiStateObserver.onEach(argWhere { it.preloader })
                uiStateObserver.onEach(argWhere { !it.preloader })
            }
            uiStateObserver.cancel()
        }

    @Test
    fun `loadDetails() shows and then hides the preloader when there is an error while loading data`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            whenever(userShowDetailsUseCase.invoke(any())).thenReturn(testFlowOf(TestException()))

            viewModel.loadDetails()

            inOrder(uiStateObserver) {
                uiStateObserver.onEach(argWhere { it.preloader })
                uiStateObserver.onEach(argWhere { !it.preloader })
            }
            uiStateObserver.cancel()
        }

    @Test
    fun `addAvatar() adds the image to the assets when correct data provided`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            val newAvatarUri = "file://avatar/uri.png"

            viewModel.addAvatar(newAvatarUri)

            verify(uiStateObserver, last()).onEach(argWhere { it.newAvatarUri == newAvatarUri })
            uiStateObserver.cancel()
        }

    @Test
    fun `addIdScan() adds the image to the assets when correct data provided`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            val newIdScanUri = "file://idScan/uri.png"

            viewModel.addIdScan(newIdScanUri)

            verify(uiStateObserver, last()).onEach(argWhere { it.newIdScanUri == newIdScanUri })
            uiStateObserver.cancel()
        }

    @Test
    fun `submit() shows the preloader and doesn't hide it when the data is correctly saved`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf())
            viewModel.loadDetails()
            val uiStateObserver = viewModel.uiState.mockObserver(this)

            viewModel.submit()

            verify(uiStateObserver, last()).onEach(argWhere { it.preloader })
            uiStateObserver.cancel()
        }

    @Test
    fun `submit() shows and then hides preloader when there is an error while loading the data`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiStateObserver = viewModel.uiState.mockObserver(this)
            whenever(userUpdateUseCase.invoke(any())).thenReturn(testFlowOf(TestException()))

            viewModel.submit()

            inOrder(uiStateObserver) {
                uiStateObserver.onEach(argWhere { it.preloader })
                uiStateObserver.onEach(argWhere { !it.preloader })
            }
            uiStateObserver.cancel()
        }

    @Test
    fun `cancel() closes screen`() =
        runTest(UnconfinedTestDispatcher()) {
            val uiEffectObserver = viewModel.uiEffect.mockObserver(this)

            viewModel.cancel()

            verify(uiEffectObserver, last()).onEach(argWhere {
                it is Routing && it.command is Back
            })
            uiEffectObserver.cancel()
        }

    companion object {
        private val DEFAULT_USER =
            User("a312b3ee-84c2-11eb-8dcd-0242ac130003", "Testnick", "test@test.com", "Test description")
        private val DEFAULT_USER_INPUT = UserUpdateUseCase.Input(
            DEFAULT_USER.id!!, DEFAULT_USER.nickname, DEFAULT_USER.email, DEFAULT_USER.description, null, null
        )
        private const val VALID_NICKNAME = "Nick1"
        private const val INVALID_NICKNAME = "TooLongNicknameOfTheUser"
        private const val VALID_EMAIL = "test@test.com"
        private const val INVALID_EMAIL = "test@test"
        private const val VALID_DESCRIPTION = "Test description"
        private const val INVALID_DESCRIPTION = "@test@"
    }
}
package com.example.simplecleanarchitecture.users.ui.userlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.RouterScreen
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.extensions.last
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.runViewModelTest
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import com.example.simplecleanarchitecture.users.ui.userlist.UserListViewModel.UiEffect.Message
import com.example.simplecleanarchitecture.users.ui.userlist.UserListViewModel.UiEffect.Routing
import com.example.simplecleanarchitecture.users.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserShowListUseCase
import com.github.terrakok.cicerone.Forward
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest : TestHelper by DefaultTestHelper() {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserListViewModel

    private lateinit var userShowListUseCase: UserShowListUseCase
    private lateinit var userDeleteUseCase: UserDeleteUseCase

    private lateinit var appResources: AppResources


    @BeforeTest
    fun setUp() {
        prepareLifecycle()
        userShowListUseCase = mock()
        userDeleteUseCase = mock()
        appResources = mock {
            on { getStringResource(R.string.common_communication_error) } doReturn "Test error message."
            on { getStringResource(R.string.user_delete_success_message) } doReturn "User deleted."
        }
        viewModel = UserListViewModel(
            userShowListUseCase,
            userDeleteUseCase,
            appResources
        )
    }

    @AfterTest
    fun tearDown() {
        cleanUpLifecycle()
        invokeViewModelOnCleared(viewModel)
    }


    @Test
    fun `loadUsers() provides the list when the proper data is loaded`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))

            viewModel.loadUsers()

            verify(stateObserver, last()).onEach(argWhere { it.userList.map { item -> item.user } == DEFAULT_USER_LIST })
        }


    @Test
    fun `loadUsers() shows the error dialog when there is an error while loading the data`() =
        runViewModelTest(viewModel) { stateObserver, effectObserver ->
            val expectedMessage = appResources.getStringResource(R.string.common_communication_error)
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(TestException()))

            viewModel.loadUsers()

            verify(stateObserver, never()).onEach(argWhere { it.userList.isNotEmpty() })
            verify(effectObserver, last()).onEach(argWhere { it is Message && it.text == expectedMessage })
        }


    @Test
    fun `loadUsers() shows and then hides the preloader when the proper data is being loaded`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))

            viewModel.loadUsers()

            inOrder(stateObserver) {
                verify(stateObserver, times(1)).onEach(argWhere { it.preloader })
                verify(stateObserver, times(1)).onEach(argWhere { !it.preloader })
            }
        }


    @Test
    fun `loadUsers() shows and then hides the preloader when there is an error while loading the data`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(TestException()))

            viewModel.loadUsers()

            inOrder(stateObserver) {
                verify(stateObserver, times(1)).onEach(argWhere { it.preloader })
                verify(stateObserver, times(1)).onEach(argWhere { !it.preloader })
            }
        }


    @Test
    fun `addNewUser() opens the user edit form`() =
        runViewModelTest(viewModel) { _, effectObserver ->
            viewModel.addNewUser()

            verify(
                effectObserver,
                times(1)
            ).onEach(argWhere { it is Routing && it.command is Forward && (it.command as Forward).screen is RouterScreen.UserEditScreen })
        }


    @Test
    fun `Given user id, when editUser(), then edit user form is opened`() =
        runViewModelTest(viewModel) { _, effectObserver ->
            viewModel.editUser(DEFAULT_USER_LIST.first().id!!)

            verify(effectObserver).onEach(argWhere {
                it is Routing && it.command is Forward &&
                        (it.command as Forward).let { command ->
                            command.screen is RouterScreen.UserEditScreen &&
                                    (command.screen as RouterScreen.UserEditScreen).id == DEFAULT_USER_LIST.first().id
                        }
            })
        }


    @Test
    fun `deleteUser() shows the confirmation dialog`() =
        runViewModelTest(viewModel) { stateObserver, effectObserver ->
            viewModel.deleteUser(DEFAULT_USER_LIST.first().id!!)

            verify(stateObserver, times(1)).onEach(argWhere { it.userActionConfirmation == DEFAULT_USER_LIST.first().id })
        }


    @Test
    fun `deleteUserConfirmed() shows the confirmation message when the user is deleted`() =
        runViewModelTest(viewModel) { _, effectObserver ->
            whenever(userDeleteUseCase.invoke(any())).thenReturn(testFlowOf())
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))
            val expectedMessage = appResources.getStringResource(R.string.user_delete_success_message)

            viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

            verify(effectObserver).onEach(argWhere { it is Message && it.text == expectedMessage })
        }


    @Test
    fun `deleteUserConfirmed() updates the list when user is deleted`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userDeleteUseCase.invoke(any())).thenReturn(testFlowOf())
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST.subList(1, 2)))

            viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

            verify(stateObserver, last()).onEach(argWhere { it.userList.map { item -> item.user } == DEFAULT_USER_LIST.subList(1, 2) })
        }


    @Test
    fun `deleteUserConfirmed() shows and then hides the preloader when user is properly deleted`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userDeleteUseCase.invoke(any())).thenReturn(testFlowOf())
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))

            viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

            inOrder(stateObserver) {
                verify(stateObserver).onEach(argWhere { it.preloader })
                verify(stateObserver).onEach(argWhere { !it.preloader })
            }
            stateObserver.cancel()
        }


    @Test
    fun `deleteUserConfirmed() shows and then hides the preloader when there was an error while deleting the user`() =
        runViewModelTest(viewModel) { stateObserver, _ ->
            whenever(userDeleteUseCase.invoke(any())).thenReturn(testFlowOf(TestException()))
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))

            viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

            inOrder(stateObserver) {
                verify(stateObserver).onEach(argWhere { it.preloader })
                verify(stateObserver).onEach(argWhere { !it.preloader })
            }
            stateObserver.cancel()
        }


    @Test
    fun `deleteUserConfirmed() displays an error message when there was an error while deleting the user`() =
        runViewModelTest(viewModel) { _, effectObserver ->
            whenever(userDeleteUseCase.invoke(any())).thenReturn(testFlowOf(TestException()))
            whenever(userShowListUseCase.invoke()).thenReturn(testFlowOf(DEFAULT_USER_LIST))

            viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

            verify(effectObserver, times(1)).onEach(argWhere { it is Message })
        }


    @Test
    fun `changeUserPassword() opens change password screen`() =
        runViewModelTest(viewModel) { _, effectObserver ->
            viewModel.changeUserPassword(DEFAULT_USER_LIST.first().id!!)

            verify(effectObserver, times(1)).onEach(argWhere {
                it is Routing && it.command == Forward(
                    RouterScreen.UserPasswordChangeScreen(
                        DEFAULT_USER_LIST.first().id!!
                    ), true
                )
            })
        }

    companion object {
        private val DEFAULT_USER_LIST = listOf(
            UserDetails(
                "a312b3ee-84c2-11eb-8dcd-0242ac130003",
                "Nickname1",
                "nickname1@test.com",
                "Test description 1"
            ),
            UserDetails(
                "3b04aacf-4320-48bb-8171-af512aae0894",
                "Nickname2",
                "nickname2@test.com",
                "Test description 1"
            ),
            UserDetails(
                "52408bc4-4cdf-49ef-ac54-364bfde3fbf0",
                "Nickname3",
                "nickname3@test.com",
                "Test description 1"
            )
        )
    }
}
@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.users.ui.userlist

import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.extensions.last
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.lib.utils.observe
import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListUiModel.Effect.OpenUserEdit
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListUiModel.Effect.OpenUserPasswordChange
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListUiModel.Effect.ShowMessage
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowListUseCase
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isA
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnitParamsRunner::class)
class UserListViewModelTest : TestHelper by DefaultTestHelper(), KoinTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { createTestDispatchers() }
                single { createTestAppResources() }
                single { mock<UserShowListUseCase>() }
                single { mock<UserDeleteUseCase>() }
                viewModel { UserListViewModel(get(), get(), get(), get()) }
            }
        )
        setMocks()
    }

    private fun setMocks(
        getUserList: () -> List<UserDetails> = { Mocks.UserDetails.listDefault },
        deleteUser: (String) -> Unit = {}
    ) {
        get<UserShowListUseCase>().stub {
            reset(mock)
            onBlocking { invoke() }.doSuspendableAnswer {
                yield()
                getUserList.invoke()
            }
        }
        get<UserDeleteUseCase>().stub {
            reset(mock)
            onBlocking { invoke(any()) }.doSuspendableAnswer {
                yield()
                deleteUser.invoke(it.arguments.first().toString())
            }
        }
    }

    private fun getViewModel(): UserListViewModel =
        get<UserListViewModel>()


    @Test
    fun `should show the user list when viewmodel is created and the proper data is loaded`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { _, stateObserver, _ ->
            advanceUntilIdle()

            verify(stateObserver, last()).onEach(argWhere { it.userList.map { item -> item.user } == Mocks.UserDetails.listDefault })
        }
    }

    @Test
    fun `should show the error dialog when there is an error while loading the data`() = runTest(StandardTestDispatcher()) {
        setMocks(getUserList = { throw TestException() })
        getViewModel().observe(this) { stateObserver, effectObserver ->
            val expectedMessage = get<AppResources>().getStringResource(R.string.common_communication_error)
            advanceUntilIdle()

            verify(stateObserver, never()).onEach(argWhere { it.userList.isNotEmpty() })
            verify(effectObserver, last()).onEach(argWhere { it is ShowMessage && it.text == expectedMessage })
        }
    }

    @Test
    fun `should show and then hide the preloader when the proper data is being loaded`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { stateObserver, _ ->
            advanceUntilIdle()

            inOrder(stateObserver) {
                verify(stateObserver, times(1)).onEach(argWhere { it.preloader })
                verify(stateObserver, times(1)).onEach(argWhere { !it.preloader })
            }
        }
    }

    @Test
    fun `should show and then hide the preloader when there is an error while loading the data`() = runTest(StandardTestDispatcher()) {
        setMocks({ throw TestException() })
        getViewModel().observe(this) { stateObserver, _ ->
            advanceUntilIdle()

            inOrder(stateObserver) {
                verify(stateObserver, times(1)).onEach(argWhere { it.preloader })
                verify(stateObserver, times(1)).onEach(argWhere { !it.preloader })
            }
        }
    }

    @Test
    fun `should open user add form when add user option is clicked`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.addNewUser()
            advanceUntilIdle()

            verify(effectObserver, times(1)).onEach(argWhere {
                it is OpenUserEdit && it.id == null
            })
        }
    }

    @Test
    fun `should open edit user form when edit user option is clicked`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.editUser(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            verify(effectObserver).onEach(argWhere {
                it is OpenUserEdit && it.id == Mocks.UserDetails.listDefault.first().id
            })
        }
    }

    @Test
    fun `should show the confirmation dialog when delete user option is clicked`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.userActionConfirmation }.mockObserver(this, true)
        val id = Mocks.UserDetails.listDefault.first().id!!
        advanceUntilIdle()

        viewModel.deleteUser(id)
        advanceUntilIdle()

        verify(observer, last()).onEach(eq(id))
        observer.cancel()
    }

    @Test
    fun `should show the confirmation message when user deletion is confirmed`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            val expectedMessage = get<AppResources>().getStringResource(R.string.user_delete_success_message)
            advanceUntilIdle()

            viewModel.deleteUserConfirmed(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            verify(effectObserver, atLeastOnce()).onEach(argWhere {
                it is ShowMessage && it.text == expectedMessage
            })
        }
    }

    @Test
    @Parameters("true", "false")
    fun `should hide the confirmation message when user deletion is canceled or confirmed`(isApproved: Boolean) = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.userActionConfirmation }.mockObserver(this, true)
        advanceUntilIdle()

        if (isApproved) {
            viewModel.cancelUserAction()
        } else {
            viewModel.cancelUserAction()
        }
        advanceUntilIdle()

        verify(observer, last()).onEach(isNull())
        observer.cancel()
    }

    @Test
    fun `should refresh user list when user is properly deleted`() = runTest(StandardTestDispatcher()) {
        setMocks(getUserList = { Mocks.UserDetails.listDefault })
        getViewModel().observe(this) { viewModel, stateObserver, _ ->
            advanceUntilIdle()
            setMocks(getUserList = { Mocks.UserDetails.listDefault.subList(1, 2) })

            viewModel.deleteUserConfirmed(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()
            advanceTimeBy(10000)

            verify(stateObserver, last()).onEach(argWhere {
                it.userList.map { item -> item.user } == Mocks.UserDetails.listDefault.subList(1, 2)
            })
        }
    }

    @Test
    fun `should show and then hide the preloader when user is properly deleted`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, stateObserver, _ ->
            advanceUntilIdle()
            viewModel.deleteUserConfirmed(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            inOrder(stateObserver) {
                verify(stateObserver).onEach(argWhere { it.preloader })
                verify(stateObserver).onEach(argWhere { !it.preloader })
            }
            stateObserver.cancel()
        }
    }

    @Test
    fun `should show and then hide the preloader when there was an error while deleting the user`() = runTest(StandardTestDispatcher()) {
        setMocks(deleteUser = { throw TestException() })
        getViewModel().observe(this) { viewModel, stateObserver, _ ->
            advanceUntilIdle()

            viewModel.deleteUserConfirmed(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            inOrder(stateObserver) {
                verify(stateObserver).onEach(argWhere { it.preloader })
                verify(stateObserver).onEach(argWhere { !it.preloader })
            }
            stateObserver.cancel()
        }
    }

    @Test
    fun `should show an error message when there was an error while deleting the user`() = runTest(StandardTestDispatcher()) {
        setMocks(deleteUser = { throw TestException() })
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()
            viewModel.deleteUserConfirmed(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            verify(effectObserver, times(1)).onEach(isA<ShowMessage>())
        }
    }

    @Test
    fun `should open change password screen when change password option is clicked`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.changeUserPassword(Mocks.UserDetails.listDefault.first().id!!)
            advanceUntilIdle()

            verify(effectObserver, times(1)).onEach(argWhere {
                it is OpenUserPasswordChange && it.id == Mocks.UserDetails.listDefault.first().id!!
            })
        }
    }
}
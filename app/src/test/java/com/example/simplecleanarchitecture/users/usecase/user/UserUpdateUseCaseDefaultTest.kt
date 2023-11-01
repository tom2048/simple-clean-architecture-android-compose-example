package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.CoroutineDefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.CoroutineTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.StorageRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class UserUpdateUseCaseDefaultTest : CoroutineTestHelper by CoroutineDefaultTestHelper() {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var userUpdateUseCase: UserUpdateUseCase

    private lateinit var storageRepository: StorageRepository
    private lateinit var usersRepository: UsersRepository
    private lateinit var assetsRepository: AssetsRepository
    private lateinit var appResources: AppResources

    @BeforeTest
    fun setupTest() {
        storageRepository = mock {
            on { load(any()) } doReturn testFlowOf("test".toByteArray())
        }
        usersRepository = mock {
            on { insert(any()) } doReturn testFlowOf(DEFAULT_USER.id!!)
            on { update(any()) } doReturn testFlowOf()
        }
        assetsRepository = mock {
            on { saveAsset(any(), any(), any(), any()) } doReturn testFlowOf("file://uri")
        }
        appResources = mock()
        userUpdateUseCase =
            UserUpdateUseCaseDefault(storageRepository, usersRepository, assetsRepository, appResources)
    }

    @AfterTest
    fun cleanupTest() {
        // Cleanup if needed
    }


    @Test
    fun `invoke() executes insert when empty user id`() = runTest(UnconfinedTestDispatcher()) {
        whenever(usersRepository.insert(any())).thenReturn(testFlowOf(DEFAULT_USER.id!!))
        whenever(usersRepository.update(any())).thenReturn(testFlowOf())
        val newUserInput = UserUpdateUseCase.Input(
            id = null,
            nickname = DEFAULT_USER.nickname,
            email = DEFAULT_USER.email,
            description = DEFAULT_USER.description,
            avatarUri = DEFAULT_USER.avatarUri,
            idScanUri = DEFAULT_USER.idScanUri
        )

        val testObserver = userUpdateUseCase(newUserInput).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(usersRepository, times(1)).insert(any())
        verify(usersRepository, never()).update(any())
    }

    @Test
    fun `invoke() executes update when empty user id`() = runTest(UnconfinedTestDispatcher()) {
        val testObserver = userUpdateUseCase(EXISTING_USER_INPUT).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(usersRepository, never()).insert(any())
        verify(usersRepository, times(1)).update(any())
    }

    @Test
    fun `invoke() saves avatar photo when avatar uri not null and new user added`() = runTest(UnconfinedTestDispatcher()) {
        val testObserver = userUpdateUseCase(ADD_ATTACHMENTS_NEW_USER_INPUT).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(assetsRepository, times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_NEW_USER_INPUT.avatarUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `invoke() saves avatar photo when avatar uri not null and existing user edited`() = runTest(UnconfinedTestDispatcher()) {
        val testObserver = userUpdateUseCase(ADD_ATTACHMENTS_EXISTING_USER_INPUT).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(assetsRepository, times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_EXISTING_USER_INPUT.avatarUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `invoke() saves id scan photo when id scan uri not null and new user added`() = runTest(UnconfinedTestDispatcher()) {
        val testObserver = userUpdateUseCase(ADD_ATTACHMENTS_NEW_USER_INPUT).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(assetsRepository, times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_NEW_USER_INPUT.idScanUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `invoke() saves id scan photo when id scan uri not null and existing user edited`() = runTest(UnconfinedTestDispatcher()) {
        val testObserver = userUpdateUseCase(ADD_ATTACHMENTS_EXISTING_USER_INPUT).mockObserver(this)

        verify(testObserver).onCompletion()
        verify(assetsRepository).saveAsset(
            eq(ADD_ATTACHMENTS_EXISTING_USER_INPUT.idScanUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }


    companion object {
        private val DEFAULT_USER = User(
            "a312b3ee-84c2-11eb-8dcd-0242ac130003",
            "Testnick",
            "test@test.com",
            "Test description"
        )

        private val EXISTING_USER_INPUT = UserUpdateUseCase.Input(
            id = DEFAULT_USER.id,
            nickname = DEFAULT_USER.nickname,
            email = DEFAULT_USER.email,
            description = DEFAULT_USER.description,
            avatarUri = DEFAULT_USER.avatarUri,
            idScanUri = DEFAULT_USER.idScanUri
        )

        private val NEW_USER_INPUT = EXISTING_USER_INPUT.copy(id = null)

        private val ADD_ATTACHMENTS_EXISTING_USER_INPUT = EXISTING_USER_INPUT.copy(
            avatarUri = "file://uri/avatar.png",
            idScanUri = "file://uri/idScan.png"
        )

        private val ADD_ATTACHMENTS_NEW_USER_INPUT = NEW_USER_INPUT.copy(
            avatarUri = "file://uri/avatar.png",
            idScanUri = "file://uri/idScan.png"
        )

    }
}
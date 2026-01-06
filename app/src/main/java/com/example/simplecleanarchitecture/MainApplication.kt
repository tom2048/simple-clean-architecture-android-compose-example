package com.example.simplecleanarchitecture

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.resources.AppResourcesDefault
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.AssetsRepositoryStorage
import com.example.simplecleanarchitecture.data.repository.FileStorageRepository
import com.example.simplecleanarchitecture.data.repository.StorageRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepositoryMemory
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditViewModel
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListViewModel
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserDeleteUseCaseDefault
import com.example.simplecleanarchitecture.domain.usecase.user.UserPasswordUpdateUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserPasswordUpdateUseCaseDefault
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowDetailsUseCaseDefault
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowListUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowListUseCaseDefault
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCaseDefault
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(
                listOf(
                    coreModule(),
                    viewModelModule(),
                    repositoryModule(),
                    useCaseModule(),
                    otherModule()
                )
            )
        }
    }

    private fun coreModule() = module {
        single { AppDispatchers() }
    }

    private fun viewModelModule() = module {
        viewModel { UserListViewModel(get(), get(), get(), get()) }
        viewModel { (userId: String, state: SavedStateHandle) ->
            UserEditViewModel(
                userId,
                state,
                get(),
                get(),
                get(),
                get()
            )
        }
        viewModel { (userId: String, state: SavedStateHandle) ->
            UserPasswordChangeViewModel(
                userId,
                state,
                get(),
                get()
            )
        }
    }

    private fun repositoryModule() = module {
        single<StorageRepository> { FileStorageRepository(get(), get()) }
        single<UsersRepository> { UsersRepositoryMemory(get()) }
        single<AssetsRepository> { AssetsRepositoryStorage(get(), get()) }
    }

    private fun useCaseModule() = module {
        single<UserShowListUseCase> { UserShowListUseCaseDefault(get()) }
        single<UserShowDetailsUseCase> { UserShowDetailsUseCaseDefault(get(), get(), get()) }
        single<UserUpdateUseCase> { UserUpdateUseCaseDefault(get(), get(), get(), get(), get()) }
        single<UserDeleteUseCase> { UserDeleteUseCaseDefault(get()) }
        single<UserPasswordUpdateUseCase> { UserPasswordUpdateUseCaseDefault(get()) }
    }

    private fun otherModule() = module {
        single<AppResources> { AppResourcesDefault(get()) }
    }

}
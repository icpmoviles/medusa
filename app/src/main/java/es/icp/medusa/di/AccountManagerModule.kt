package es.icp.medusa.di

import android.accounts.AccountManager
import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AccountManagerModule {

    @Provides
    @Singleton
    fun provideAccountManager(application: Application): AccountManager {
        return AccountManager.get(application)
    }

}
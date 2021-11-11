package com.example.flashsports.di


import com.example.flashsports.data.repositories.LoginRepository
import com.example.flashsports.data.repositories.UserRepository
import com.example.flashsports.utils.DataUtils.getUserRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideLoginRepository(): LoginRepository = LoginRepository()

    @Singleton
    @Provides
    fun provideUserRepository(): UserRepository = getUserRepo()


}
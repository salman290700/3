package com.example.dietjoggingapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.grpc.Context.Storage
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

//    @Provides
//    @Singleton
//    fun provideFirebsaeStorageReference(): StorageReference {
//        var storageReference = StorageReference().root
//        return storageReference
//    }
    @Provides
    @Singleton
    fun StorageReference(): StorageReference {
        var storageReference = FirebaseStorage.getInstance().reference.root
        return storageReference
    }
}
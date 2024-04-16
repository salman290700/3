package com.example.dietjoggingapp.repositories

import android.net.Uri
import android.util.Log
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JoggingRepoImp(
    private val database:FirebaseFirestore,
    private val storageReference:StorageReference
): JoggingRepo {
    override fun getJoggings(userId: String, result: (UiState<List<Jogging>>) -> Unit) {
        database.collection(Constants.FirestoreTable.JOGGING)
            .whereEqualTo(Constants.FireStoreDocumentField.USER_ID, userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                val projects = arrayListOf<Jogging>()
                Log.d("TAG", "getProjects: " + userId.trim())
                for (document in it){
                    val project = document.toObject(Jogging::class.java)
                    projects.add(project)
                    Log.d("TAG", "getJoggings: ${project.toString().trim()}")
                }
                result.invoke(
                    UiState.Success(projects)
                )
                Log.d("TAG", "getProjects: ${userId.trim()}")
            }
            .addOnFailureListener{
                result.invoke(
                    UiState.failure(
                        it.localizedMessage.toString()
                    )
                )
                Log.e("TAG", "getProjects: ${userId.trim()} ${it.localizedMessage.toString().trim()}")
            }
    }

    override fun addJogging(jogging: Jogging, result: (UiState<Pair<Jogging, String>>) -> Unit) {
        val document = database.collection(Constants.FirestoreTable.JOGGING).document()
        jogging.id = document.id
        Log.d("TAG", "addJogging: " + jogging.id)
        Log.d("TAG", "addJogging: addJogging" + jogging.caloriesBurned)
        document.set(jogging)
            .addOnSuccessListener {
                Log.d("TAG", "addJogging: distance in meters " + jogging.distanceInMeters)
                Log.d("TAG", "addJogging: calories burned" + jogging.caloriesBurned)

                result.invoke(
                    UiState.Success(Pair(jogging, "Jogging has been Added..."))
                )
                Log.d("TAG", "addJogging: " + it.toString())
            }
            .addOnFailureListener{
                result.invoke(
                    UiState.failure(it.localizedMessage.toString())
                )
                Log.d("TAG", "addJogging: " + it.localizedMessage)
            }
    }

    override fun updateJogging(jogging: Jogging, result: (UiState<String>) -> Unit) {
        val document = database.collection(Constants.FirestoreTable.JOGGING).document(jogging.id)
        document
            .set(jogging)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Jogging has been updated")
                )
            }
            .addOnFailureListener{
                result.invoke(
                    UiState.failure(
                        it.localizedMessage
                    )
                )
            }
    }

    override suspend fun uploadSingleFile(fileUri: Uri, onResult: (UiState<Uri>) -> Unit) {
        try {
            val uri: Uri = withContext(Dispatchers.IO){
                storageReference.child(Constants.FirebaseStorageConstants.PROJECT_PHOTO)
                    .putFile(fileUri)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
            }
        }
        catch (e: FirebaseException) {
            onResult.invoke(
                UiState.failure(e.message.toString())
            )
        }
        catch (e: java.lang.Exception) {
            onResult.invoke(
                UiState.failure(e.message.toString())
            )
        }
    }

    override suspend fun uploadMultipleFile(
        fileUri: List<Uri>,
        onResult: (UiState<List<Uri>>) -> Unit,
    ) {
        try{
            val uri: List<Uri> = withContext(Dispatchers.IO){
                fileUri.map { image ->
                    async { storageReference.child(Constants.FirebaseStorageConstants.PROJECT_PHOTO).child(image.lastPathSegment ?: "${System.currentTimeMillis()}")
                        .putFile(image)
                        .await()
                        .storage
                        .downloadUrl
                        .await() }
                }.awaitAll()
            }
            onResult.invoke(UiState.Success(uri))
        }catch (e: FirebaseException){
            onResult.invoke(UiState.failure(e.localizedMessage))
        }catch (e: Exception){
            onResult.invoke(UiState.failure(e.localizedMessage))
        }
    }
}
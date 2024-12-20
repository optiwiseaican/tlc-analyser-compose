package com.aican.tlcanalyzer.data.repository.auth

import com.aican.tlcanalyzer.domain.model.auth.SignUpModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AuthRepositoryImplementation @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val databaseReference: DatabaseReference
) :
    AuthRepository {
    override suspend fun signUp(name: String, email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            val today: Calendar = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)

            val currentTime = Calendar.getInstance().time
            val currentDate = Date()


            val dateFormat =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val formattedDate: String = dateFormat.format(currentDate)


            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val second = calendar[Calendar.SECOND]

            val formattedTime =
                String.format("%02d:%02d:%02d", hour, minute, second)


            val signUpModel = SignUpModel(
                name,
                email,
                firebaseAuth.currentUser?.uid.toString(),
                today.time.toString(),
                System.currentTimeMillis().toString(),
                formattedDate,
                formattedDate,
                formattedDate,
                formattedDate,
                formattedTime,
                10,
                10, password
            )

            databaseReference.child("Users")
                .child(firebaseAuth.currentUser?.uid.toString())
                .setValue(signUpModel).await()


            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)

        }
    }

}
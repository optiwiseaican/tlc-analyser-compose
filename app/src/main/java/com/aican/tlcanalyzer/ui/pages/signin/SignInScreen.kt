package com.aican.tlcanalyzer.ui.pages.signin

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aican.tlcanalyzer.domain.model.auth.AuthState
import com.aican.tlcanalyzer.ui.components.buttons.CircularRadiusButton
import com.aican.tlcanalyzer.ui.components.buttons.OutlinedRadiusButton
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBarNavigation
import com.aican.tlcanalyzer.ui.pages.common_components.EditTextFieldWithLabel
import com.aican.tlcanalyzer.viewmodel.auth.AuthViewModel

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authState: AuthState = authViewModel.authState

    Column {
        CustomTopBarNavigation(titleText = "Login") {

        }

        Spacer(modifier = Modifier.height(15.dp))

        EditTextFieldWithLabel(text = email, hint = "Enter email", label = "Email") {
            email = it

        }

        Spacer(modifier = Modifier.height(5.dp))

        EditTextFieldWithLabel(text = password, hint = "Enter password", label = "Password") {
            password = it
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot Password?",
            style = TextStyle(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 15.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))



        CircularRadiusButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp),
            text = "Login"
        ) {
            authViewModel.signIn(email, password)

        }
        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            else -> {}
        }

        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Success -> {
                    Toast.makeText(
                        context,
                        authState.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    onSignInSuccess.invoke()

                }

                is AuthState.Error -> Toast.makeText(context, authState.message, Toast.LENGTH_SHORT)
                    .show()

                else -> {}
            }
        }

        OutlinedRadiusButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp),
            text = "Sign Up"
        ) {
            onSignUpClick.invoke()
        }
    }


}
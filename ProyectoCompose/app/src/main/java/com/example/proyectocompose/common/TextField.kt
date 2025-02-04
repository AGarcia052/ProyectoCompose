package com.example.proyectocompose.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.proyectocompose.R

@Composable
fun TFBasic(
    value: String,
    label: String,
    onTextChange: (String) -> Unit,
){

    TextField(
        onValueChange = onTextChange,
        value = value,
        label = {Text(text=label)}
        )

}

@Composable
fun TFPasswd(
    passwd: String,
    label: String = "Contraseña",
    onPasswordChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    TextField(
        value = passwd,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Icon(
                painter = painterResource(if (isPasswordVisible) (R.drawable.ic_passwd_show) else (R.drawable.ic_passd_hidden)),
                contentDescription = "Contraseña",
                modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }
            )
        }
    )
}
package com.aican.tlcanalyzer.ui.pages.dashboard

import EnterProjectDetailsDialog
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.activities.NewCameraActivity
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBarNavigation
import com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.DashboardProjectView
import com.aican.tlcanalyzer.utils.AppUtils
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var showCustomDialog by remember {
        mutableStateOf(false)
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item") },
                    selected = false,
                    onClick = { }
                )
            }


        },
    ) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    containerColor = AppUtils.UiColor1,
                    text = { Text("Create", color = Color.White) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "", tint = Color.White) },
                    onClick = {
                        scope.launch {
                            showCustomDialog = !showCustomDialog

                        }
                    }
                )
            }
        ) { contentPadding ->
            Column {
                CustomTopBarNavigation(
                    titleText = "Dashboard",
                    drawable = R.drawable.hamburger
                ) {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
                Column(modifier = Modifier.padding(contentPadding)) {
                    DashboardProjectView()
                }
            }

            if (showCustomDialog) {


                EnterProjectDetailsDialog(
                    modifier = Modifier.clickable { },
                    onDismiss = {
                        showCustomDialog = false
                    },
                    onSaveClick = { name, description ->
                        showCustomDialog = false
                        val intent = Intent(context, NewCameraActivity::class.java).apply {
                            putExtra("projectName", name)
                            putExtra("projectDescription", description)
                        }
                        context.startActivity(intent)
                    },
                    projectName = "",
                    projectDescription = ""
                )


            }

        }
    }
}
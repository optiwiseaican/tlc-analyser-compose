package com.aican.tlcanalyzer.ui.pages.dashboard

import EnterProjectDetailsDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.ui.activities.NewCameraActivity
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBarNavigation
import com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.DashboardProjectView
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var showCustomDialog by remember {
        mutableStateOf(false)
    }


    val allProjectList by projectViewModel.projects.collectAsState()


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
                    DashboardProjectView(allProjectList)
                }
            }

            if (showCustomDialog) {


                EnterProjectDetailsDialog(
                    modifier = Modifier.clickable { },
                    onDismiss = {
                        showCustomDialog = false
                    },
                    onSaveClick = { name, description ->
                        if (name.isNotEmpty()) {
                            showCustomDialog = false
                            val intent = Intent(context, NewCameraActivity::class.java).apply {
                                putExtra("projectName", name)
                                putExtra("projectDescription", description)
                            }
                            context.startActivity(intent)
                        }else{
                            Toast.makeText(context, "Enter project name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    projectName = "",
                    projectDescription = ""
                )


            }

        }
    }
}
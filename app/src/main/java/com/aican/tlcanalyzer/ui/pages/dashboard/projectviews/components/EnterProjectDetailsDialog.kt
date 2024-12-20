import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.ui.components.buttons.CircularRadiusButton
import com.aican.tlcanalyzer.ui.pages.common_components.EditTextFieldWithLabel

@Composable
fun EnterProjectDetailsDialog(
    modifier: Modifier = Modifier,
    projectName: String,
    projectDescription: String,
    onDismiss: () -> Unit,
    onSaveClick: (String, String) -> Unit
) {

    var projectName by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {},
        dismissButton = {},
        text = {
            Surface(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(1.dp)
                ) {

                    EditTextFieldWithLabel(
                        text = projectName,
                        hint = "Enter project name",
                        label = "Name"
                    ) {
                        projectName = it

                    }
                    Spacer(modifier = Modifier.height(5.dp))


                    EditTextFieldWithLabel(
                        text = projectDescription,
                        hint = "Enter project description",
                        label = "Description"
                    ) {
                        projectDescription = it

                    }

                    Spacer(modifier = Modifier.height(5.dp))



                    CircularRadiusButton(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp),
                        text = "Save"
                    ) {
                        onSaveClick.invoke(projectName, projectDescription)
                    }
                }
            }
        }
    )
}

package com.example.mpntproject


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase

const val MAIN_ROUTE = "Info"
const val NEWS_ROUTE = "AddNewsInfo"




@Composable
fun Mainview() {
    val userVM = viewModel<LoginAndRegister>()
    if (userVM.username.value.isEmpty()) {
        LoginView(userVM)
    }
    else
    {
        ScaffoldView()
    }

}

@Composable
fun ScaffoldView()
{
    val navigation = rememberNavController()
    val userVM = viewModel<LoginAndRegister>()
    Scaffold(
        topBar = {Header(userVM, navigation )},
        content = { MainNavigation(navigation) },
        bottomBar = { Footer(navigation)})
}



@Composable
fun Header(Logout:LoginAndRegister,navController: NavHostController )
{
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Cyan)
        .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,


    ) {
        Text("Sky News",Modifier
            .padding(horizontal = 5.dp)
            .clickable { navController.navigate(MAIN_ROUTE) },
            fontWeight = Bold)

        Text("Searchbar not implemented")
        Text("Logout", Modifier
            .clickable { Logout.logout()},
        fontWeight = Bold)
    }
}
@Composable
fun MainNavigation(navController: NavHostController)
{
    val commentVM = viewModel<AddComment>()
    val userVM = viewModel<LoginAndRegister>()
    NavHost(navController = navController, startDestination = MAIN_ROUTE )
    {
        composable( route = MAIN_ROUTE ){ MainContentView() }
        composable( route = NEWS_ROUTE){ AddNewsComment(commentVM, userVM) }
    }
}

@Composable
fun MainContentView()
{
    var title by remember { mutableStateOf("")}
    var shortInfo by remember { mutableStateOf("") }

    val fireStore = Firebase.firestore


    Column(modifier = Modifier
        .padding(5.dp)
        .height(500.dp)
        .width(500.dp)
        .border(1.dp, Color.Black),
    horizontalAlignment = Alignment.CenterHorizontally)
    {
        //Gets info from database
        fireStore
            .collection("News")
            .get()
            .addOnSuccessListener {
                for(doc in it)
                {
                 title = doc.get("Title").toString()
                    shortInfo = doc.get("Short Info").toString()
                }
            }
        Text(text = title, Modifier
            .padding(20.dp),
             fontWeight = ExtraBold, fontSize = 30.sp)
        Image(painter = painterResource(id = R.drawable.war) , contentDescription ="",Modifier.size(350.dp), contentScale = ContentScale.FillHeight)
        Text(text = shortInfo, Modifier.padding(20.dp))



    }


}
@Composable
fun Footer(navController: NavHostController)
{
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Cyan)
        .padding(20.dp)
        ,
        horizontalArrangement = Arrangement.SpaceEvenly) {
      Icon(painter = painterResource(id = R.drawable.ic_homefeed ),
          contentDescription ="Info",
          modifier = Modifier.clickable { navController.navigate(MAIN_ROUTE) })
        Icon(painter = painterResource(id = R.drawable.icon_add), contentDescription = "AddNewsInfo",
        Modifier.clickable { navController.navigate(NEWS_ROUTE)  })
    }


}

@Composable
fun AddNewsComment(commentVM: AddComment, userVM: LoginAndRegister){
   var commentText by remember { mutableStateOf("") }
    val fireStore = Firebase.firestore
    val doc = mapOf<String, String>(
        "message" to commentText

    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp))
    {
        Text(text= "You are user: "+userVM.username.value)
        Text(text= "Press add comment to add an comment or press remove after writing which comment to delete.")
        OutlinedTextField(value = commentText
            , onValueChange ={commentText=it}
            , label = { Text(text = "Add or delete comment ")} )

        OutlinedButton(onClick = { if (commentText.isNotEmpty()){commentVM.addComment(Comment(commentText)); fireStore
            .collection("comments")
            .document(userVM.username.value)
            .set(doc)} else {}}, Modifier.padding(top = 5.dp))
        {
            Text("Add comment")
        }
        OutlinedButton(onClick = { if (commentText.isNotEmpty()){commentVM.deleteComment(Comment(commentText))} else {}}, Modifier.padding(top = 5.dp))
        {
            Text("Delete")
        }
        commentVM.comments.value.forEach{
        Text(text = it.comment +"                                  Comment by user, "+ userVM.username.value, modifier = Modifier.padding(top=25.dp))
        }
    }
    
    
    
    
}

@Composable
fun LoginView(userVM: LoginAndRegister){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(5.dp)
            .height(500.dp)
            .width(500.dp)
            .border(width = 1.dp, Color.Black),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text("Login to access content or register to add user")
        OutlinedTextField(
            value = email ,
            onValueChange = { email = it },
            label = { Text(text = "Email") })
        OutlinedTextField(
            value = password ,
            onValueChange = { password = it },
            label = { Text(text = "Password (More than 5 characters)") },
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedButton(onClick = { if (email.isNotEmpty() && password.isNotEmpty()){userVM.login(email,password)} else {println("Null or empty text fields")} }) {
            Text(text = "Login") }

        OutlinedButton(onClick = { if (email.isNotEmpty() && password.isNotEmpty()){userVM.register(email,password)}else {println("Null or empty text fields")}}) {
            Text(text = "Register") }

    }
}




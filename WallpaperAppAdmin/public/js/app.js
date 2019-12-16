var firebaseConfig = {
    apiKey: "AIzaSyCzwknIoXfKzc71ESzv6ZNimD7S6T4sMd8",
    authDomain: "mywallpaperapp-b591f.firebaseapp.com",
    databaseURL: "https://mywallpaperapp-b591f.firebaseio.com",
    projectId: "mywallpaperapp-b591f",
    storageBucket: "mywallpaperapp-b591f.appspot.com",
    messagingSenderId: "162268346865",
    appId: "1:162268346865:web:19258b4942ce619ade6c50"
  };
firebase.initializeApp( firebaseConfig);
firebase.auth.Auth.Persistence.LOCAL;

$("#btn-login").click(function(){
    var email = $("#email").val();
    var password = $("#password").val();

    var result = firebase.auth().signInWithEmailAndPassword(email, password);

    result.catch(function(error){
        var errorCode = error.code;
        var errorMessage = error.message;

        console.log(errorCode);
        console.log(errorMessage);
    });
});



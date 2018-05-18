package cs.com.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cs.com.project.models.User;


/**
 * This screen is where the user picks how they want to log in. It's shown the first time the user
 * opens the app
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "Project";
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 0;


    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentFirebaseUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentFirebaseUser);
    }

    private void updateUI(FirebaseUser firebaseUser) {
        hideProgressDialog();

        if (firebaseUser == null) { // we don't have a firebase user yet, attempt logins.
            View loginContainer = findViewById(R.id.login_container);
            loginContainer.setVisibility(View.VISIBLE);
            View googleLoginButton = loginContainer.findViewById(R.id.google_sign_in_button);
            googleLoginButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signInWithGoogleAndAuthenticateWithFirebase();
                        }
                    });

        } else {
            View loginContainer = findViewById(R.id.login_container);
            loginContainer.setVisibility(View.GONE);
            goToMainActivity();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Log.i(TAG, "Returned from Google sign in with " + (resultCode == Activity.RESULT_OK ? "SUCCESS" : "NOT SUCCESS"));
            Log.i(TAG, "... going to try get google account to authenticate with firebase");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authenticateFirebaseWithGoogleUser(account);
            } catch(ApiException e) {
                Log.e(TAG, "Error trying to get google user", e);
                updateUI(null);
            }
        }
    }

    private void signInWithGoogleAndAuthenticateWithFirebase() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void authenticateFirebaseWithGoogleUser(GoogleSignInAccount googleUser) {
        Log.i(TAG, "Authenticating firebase with google user: " + googleUser.getDisplayName());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(googleUser.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(
                        this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(findViewById(R.id.root_layout), "Successfully authenticated with Firebase", Snackbar.LENGTH_SHORT).show();
                                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                    addFirebaseUserToUsersDataset();
                                    updateUI(firebaseUser);

                                } else {
                                    Log.e(TAG, "Failed to authenticate firebase with google user", task.getException());
                                    Snackbar.make(findViewById(R.id.root_layout), "Firebase authentication failed", Snackbar.LENGTH_SHORT).show();

                                    updateUI(null);
                                }

                                hideProgressDialog();
                            }
                        });
    }

    private void addFirebaseUserToUsersDataset() {
        final String uid = mFirebaseAuth.getCurrentUser().getUid();
        String displayName = mFirebaseAuth.getCurrentUser().getDisplayName();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference();
        User user = new User(uid, displayName);
        databaseReference.child(FirebaseConstants.USERS).child(uid).setValue(user);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

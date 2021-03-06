package com.rajatgoyal.puzzle15.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.adapter.HighScoresAdapter;
import com.rajatgoyal.puzzle15.model.GamePlay;
import com.rajatgoyal.puzzle15.task.HighScoreFetchTask;
import com.rajatgoyal.puzzle15.util.AchievementHandler;
import com.rajatgoyal.puzzle15.util.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;

/**
 * Created by rajat on 15/9/17.
 */

public class MainActivity extends BaseActivity {

	private static final int RC_GAMES_ACHIEVEMENTS = 9003;

	private Button loginButton, resumeBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
	}

	@Override
	protected void onResume() {
		super.onResume();

		boolean resumeGame = SharedPref.getResumeFlag();
		if (resumeGame) {
			resumeBtn.setVisibility(View.VISIBLE);
		} else {
			resumeBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void signInSilently() {
		super.signInSilently();
	}

	@Override
	protected void startSignInIntent() {
		super.startSignInIntent();
	}

	@Override
	protected void onConnected(GoogleSignInAccount googleSignInAccount) {
		super.onConnected(googleSignInAccount);
		updateLoginButton();

		super.getPlayersClient()
				.getCurrentPlayer()
				.addOnCompleteListener(new OnCompleteListener<Player>() {
					@Override
					public void onComplete(@NonNull Task<Player> task) {
						String displayName;
						if (task.isSuccessful() && task.getResult() != null) {
							displayName = task.getResult().getDisplayName();
						} else {
							displayName = "???";
						}
						Timber.d("User signed in with display name: %s", displayName);
					}
				});
	}

	@Override
	public void onDisconnected() {
		super.onDisconnected();
		updateLoginButton();
	}

	public void updateLoginButton() {
		loginButton.setText(isSignedIn() ? R.string.signOut : R.string.signIn);
	}

	public void init() {
		resumeBtn = findViewById(R.id.resume_game);
		resumeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGameActivity();
			}
		});


		Button newGame = findViewById(R.id.new_game);
		newGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean resumeGame = SharedPref.getResumeFlag();
				if (resumeGame) {
					Resources resources = getResources();

					String dialogTitle = resources.getString(R.string.restart_game);
					String dialogYesText = resources.getString(R.string.yes);
					String dialogNoText = resources.getString(R.string.no);
					DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPref.setResumeFlag(false);
							startGameActivity();
						}
					};
					DialogInterface.OnClickListener noListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					};
					showAlert(dialogTitle, dialogYesText, yesListener, dialogNoText, noListener);

				} else {
					startGameActivity();
				}

			}
		});

		Button highScore = findViewById(R.id.high_score);
		highScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHighScore();
			}
		});

		Button help = findViewById(R.id.help);
		help.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRules();
			}
		});

		loginButton = findViewById(R.id.login);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) signOut();
				else startSignInIntent();
			}
		});

		Button viewAchievements = findViewById(R.id.view_achievements);
		viewAchievements.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAchievements();
			}
		});
	}

	public void showAchievements() {
		if (!isSignedIn()) {
			View layout = findViewById(R.id.main_layout);
			Snackbar.make(layout, "Please sign in first", Snackbar.LENGTH_SHORT).show();
			return;
		}
		Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getAchievementsIntent()
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						startActivityForResult(intent, RC_GAMES_ACHIEVEMENTS);
					}
				});
	}

	public void startGameActivity() {
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}

	private void showAlert(String title, String yesText,
						   DialogInterface.OnClickListener yesListener, String noText,
						   DialogInterface.OnClickListener noListener) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setPositiveButton(yesText, yesListener);
		alertDialogBuilder.setNegativeButton(noText, noListener);

		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
	}

	@SuppressLint("StaticFieldLeak")
	public void showHighScore() {
		new HighScoreFetchTask(this) {
			@Override
			protected void onPostExecute(ArrayList<GamePlay> gamePlays) {
				super.onPostExecute(gamePlays);
				if (gamePlays == null || gamePlays.size() == 0) {
					View layout = findViewById(R.id.main_layout);
					Snackbar.make(layout, "No high scores yet", Snackbar.LENGTH_SHORT).show();
				} else {
					openHighScoresDialog(gamePlays);
				}
			}
		}.execute();
	}

	public void openHighScoresDialog(ArrayList<GamePlay> gamePlays) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.dialog_high_score, null);

		RecyclerView highScoresList = view.findViewById(R.id.high_scores_list);

		LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
		highScoresList.setLayoutManager(layoutManager);

		HighScoresAdapter adapter = new HighScoresAdapter(gamePlays);

		highScoresList.setAdapter(adapter);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void showRules() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(R.layout.dialog_game_rules);

		AlertDialog dialog = builder.create();
		dialog.show();

		AchievementHandler achievementHandler = getAchievementHandler();
		if (achievementHandler != null) achievementHandler.unlockRulesAchievements(this);
	}
}

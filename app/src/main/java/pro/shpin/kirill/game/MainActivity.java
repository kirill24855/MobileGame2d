package pro.shpin.kirill.game;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

	private GLSurfaceView glSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Game game = new Game();
		glSurfaceView = new GameView(this, game);

		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (!supportsEs2) return;
		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setRenderer(new ESRenderer(game));

		// No title bar
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Fullscreen
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		glSurfaceView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				|   View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				|   View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				|   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				|   View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				|   View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		);

		setContentView(glSurfaceView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glSurfaceView.onPause();
	}
}

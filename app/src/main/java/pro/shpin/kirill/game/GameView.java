package pro.shpin.kirill.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {

	private Game game;

	public GameView(Context context) {
		super(context);
	}

	public GameView(Context context, Game game) {
		super(context);
		this.game = game;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		game.setTouchCoords(e.getX(), e.getY(), getWidth(), getHeight());
		return true;
	}
}

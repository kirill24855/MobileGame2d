package pro.shpin.kirill.game;

import android.util.Log;

public class Game {

	private float touchX = 0;
	private float touchY = 0;

	public void init() {

	}

	public void update(float interval) {

	}

	public void cleanup() {

	}

	public void setTouchCoords(float x, float y, float width, float height) {
		this.touchX = x/width*2f-1f;
		this.touchY = -(y/height*2f-1f);
	}

	public float getTouchX() {
		return touchX;
	}

	public float getTouchY() {
		return touchY;
	}
}

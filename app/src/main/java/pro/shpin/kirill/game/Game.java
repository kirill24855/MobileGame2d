package pro.shpin.kirill.game;

public class Game {

	private float touchX = 0;
	private float touchY = 0;

	public Game() {

	}

	public void setTouchCoords(float x, float y, float width, float height) {
		this.touchX = x/width*2-1;
		this.touchY = -(y/height*2-1);
	}

	public float getTouchX() {
		return touchX;
	}

	public float getTouchY() {
		return touchY;
	}
}

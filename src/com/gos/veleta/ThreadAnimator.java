package com.gos.veleta;

import android.view.View;
import android.view.animation.RotateAnimation;

public class ThreadAnimator {

	final Thread animator;
	static final int ADJUST_X = 5;
	static final int ANIMATION_DURATION_MIL = 2 * 1000;
	static final int ANIMATION_TOTAL_LAPS = 4;

	public ThreadAnimator(final View view) {

		animator = new Thread() {

			@Override
			public void run() {

				RotateAnimation animation = new RotateAnimation(0,
						360 * ANIMATION_TOTAL_LAPS, view.getWidth() / 2
								+ ADJUST_X, view.getHeight() / 2);
				animation.setDuration(ANIMATION_DURATION_MIL);

				view.setVisibility(View.VISIBLE);
				view.startAnimation(animation);
			}
		};

	}

	public void startAnimation() {
		animator.start();
	}

	public void awaitCompletion() throws InterruptedException {
		animator.join();
	}
}

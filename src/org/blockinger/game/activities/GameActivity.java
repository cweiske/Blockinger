/*
b * Copyright 2013 Simon Willeke
 * contact: hamstercount@hotmail.com
 */

/*
    This file is part of Blockinger.

    Blockinger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Blockinger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Blockinger.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.blockinger.game.activities;

import org.blockinger.game.BlockBoardView;
import org.blockinger.game.R;
import org.blockinger.game.WorkThread;
import org.blockinger.game.components.Controls;
import org.blockinger.game.components.Display;
import org.blockinger.game.components.GameState;
import org.blockinger.game.components.Sound;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.view.InputDevice;


public class GameActivity extends FragmentActivity {

	public Sound sound;
	public Controls controls;
	public Display display;
	public GameState game;
	private WorkThread mainThread;
	private DefeatDialogFragment dialog;
	private boolean layoutSwap;

	public static final int NEW_GAME = 0;
	public static final int RESUME_GAME = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false)) {
			setContentView(R.layout.activity_game_alt);
			layoutSwap = true;
		} else {
			setContentView(R.layout.activity_game);
			layoutSwap = false;
		}

		/* Read Starting Arguments */
		Bundle b = getIntent().getExtras();
		int value = NEW_GAME;
		
		/* Create Components */
		game = (GameState)getLastCustomNonConfigurationInstance();
		if(game == null) {
			/* Check for Resuming (or Resumption?) */
			if(b!=null)
				value = b.getInt("mode");
				
			if((value == NEW_GAME)) {
				game = GameState.getNewInstance(this);
				game.setLevel(b.getInt("level"));
			} else
				game = GameState.getInstance(this);
		}
		game.reconnect(this);
		dialog = new DefeatDialogFragment();
		controls = new Controls(this);
		display = new Display(this);
		sound = new Sound(this);
		
		/* Init Components */
		if(game.isResumable())
			sound.startMusic(Sound.GAME_MUSIC, game.getSongtime());
		sound.loadEffects();
		if(b!=null){
			value = b.getInt("mode");
			if(b.getString("playername") != null)
				game.setPlayerName(b.getString("playername"));
		} else 
			game.setPlayerName(getResources().getString(R.string.anonymous));
		dialog.setCancelable(false);
		if(!game.isResumable())
			gameOver(game.getScore(), game.getTimeString(), game.getAPM());
		
		/* Register Button callback Methods */
		((Button)findViewById(R.id.pausebutton_1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GameActivity.this.finish();
			}
		});
		((BlockBoardView)findViewById(R.id.boardView)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.boardPressed(event.getX(), event.getY());
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.boardReleased();
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rightButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rightButtonPressed();
		        	((ImageButton)findViewById(R.id.rightButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rightButtonReleased();
		        	((ImageButton)findViewById(R.id.rightButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.leftButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.leftButtonPressed();
		        	((ImageButton)findViewById(R.id.leftButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.leftButtonReleased();
		        	((ImageButton)findViewById(R.id.leftButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.softDropButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.downButtonPressed();
		        	((ImageButton)findViewById(R.id.softDropButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.downButtonReleased();
		        	((ImageButton)findViewById(R.id.softDropButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.hardDropButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.dropButtonPressed();
		        	((ImageButton)findViewById(R.id.hardDropButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.dropButtonReleased();
		        	((ImageButton)findViewById(R.id.hardDropButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rotateRightButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rotateRightPressed();
		        	((ImageButton)findViewById(R.id.rotateRightButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rotateRightReleased();
		        	((ImageButton)findViewById(R.id.rotateRightButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rotateLeftButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rotateLeftPressed();
		        	((ImageButton)findViewById(R.id.rotateLeftButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rotateLeftReleased();
		        	((ImageButton)findViewById(R.id.rotateLeftButton)).setPressed(false);
		        }
		        return true;
		    }
		});

		((BlockBoardView)findViewById(R.id.boardView)).init();
		((BlockBoardView)findViewById(R.id.boardView)).setHost(this);
		if (this.hasGameController()) {
			//hide on-screen control buttons if we have a gamepad
			((ImageButton)findViewById(R.id.rightButton)).setVisibility(View.INVISIBLE);
			((ImageButton)findViewById(R.id.leftButton)).setVisibility(View.INVISIBLE);
			((ImageButton)findViewById(R.id.softDropButton)).setVisibility(View.INVISIBLE);
			((ImageButton)findViewById(R.id.hardDropButton)).setVisibility(View.INVISIBLE);
			((ImageButton)findViewById(R.id.rotateRightButton)).setVisibility(View.INVISIBLE);
			((ImageButton)findViewById(R.id.rotateLeftButton)).setVisibility(View.INVISIBLE);
			((Button)findViewById(R.id.pausebutton_1)).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				controls.downButtonPressed();
				return true;

			case KeyEvent.KEYCODE_DPAD_LEFT:
				controls.leftButtonPressed();
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				controls.rightButtonPressed();
				return true;

			case KeyEvent.KEYCODE_DPAD_UP:
				controls.rotateRightPressed();
				return true;

			case KeyEvent.KEYCODE_BUTTON_A:
				// OUYA: O
				controls.dropButtonPressed();
				return true;

			case KeyEvent.KEYCODE_BUTTON_X:
				// OUYA: U
				controls.rotateLeftPressed();
				return true;

			case KeyEvent.KEYCODE_BUTTON_B:
				// OUYA: A
				controls.rotateRightPressed();
				return true;

			case KeyEvent.KEYCODE_MENU:
				// pause
				GameActivity.this.finish();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				controls.downButtonReleased();
				return true;

			case KeyEvent.KEYCODE_DPAD_LEFT:
				controls.leftButtonReleased();
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				controls.rightButtonReleased();
				return true;

			case KeyEvent.KEYCODE_DPAD_UP:
				controls.rotateRightReleased();
				return true;

			case KeyEvent.KEYCODE_BUTTON_A:
				// OUYA: O
				controls.dropButtonReleased();
				return true;

			case KeyEvent.KEYCODE_BUTTON_X:
				// OUYA: U
				controls.rotateLeftReleased();
				return true;

			case KeyEvent.KEYCODE_BUTTON_B:
				// OUYA: A
				controls.rotateRightReleased();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * Called by BlockBoardView upon completed creation
	 * @param caller
	 */
	public void startGame(BlockBoardView caller){
		mainThread = new WorkThread(this, caller.getHolder()); 
		mainThread.setFirstTime(false);
		game.setRunning(true);
		mainThread.setRunning(true);
		mainThread.start();
	}

	/**
	 * Called by BlockBoardView upon destruction
	 */
	public void destroyWorkThread() {
        boolean retry = true;
        mainThread.setRunning(false);
        while (retry) {
            try {
            	mainThread.join();
                retry = false;
            } catch (InterruptedException e) {
                
            }
        }
	}
	
	/**
	 * Called by GameState upon Defeat
	 * @param score
	 */
	public void putScore(long score) {
		String playerName = game.getPlayerName();
		if(playerName == null || playerName.equals(""))
			playerName = getResources().getString(R.string.anonymous);//"Anonymous";
		
		Intent data = new Intent();
		data.putExtra(MainActivity.PLAYERNAME_KEY, playerName);
		data.putExtra(MainActivity.SCORE_KEY, score);
		setResult(MainActivity.RESULT_OK, data);
		
		finish();
	}
	
	@Override
	protected void onPause() {
    	super.onPause();
    	sound.pause();
    	sound.setInactive(true);
    	game.setRunning(false);
	};
    
    @Override
    protected void onStop() {
    	super.onStop();
    	sound.pause();
    	sound.setInactive(true);
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	game.setSongtime(sound.getSongtime());
    	sound.release();
    	sound = null;
    	game.disconnect();
    };
    
    @Override
    protected void onResume() {
    	super.onResume();
    	sound.resume();
    	sound.setInactive(false);
    	
    	/* Check for changed Layout */
    	boolean tempswap = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false);
		if(layoutSwap != tempswap) {
			layoutSwap = tempswap;
			if(layoutSwap) {
				setContentView(R.layout.activity_game_alt);
			} else {
				setContentView(R.layout.activity_game);
			}
		}
    	game.setRunning(true);
    };
    
    @Override
    public Object onRetainCustomNonConfigurationInstance () {
        return game;
    }
	
	public void gameOver(long score, String gameTime, int apm) {
		dialog.setData(score, gameTime, apm);
		dialog.show(getSupportFragmentManager(), "hamster");
	}

	/**
	 * Check if a game controller (gamepad/joystick) is connected
	 */
	protected boolean hasGameController() {
		int[] deviceIds = InputDevice.getDeviceIds();
		for (int deviceId : deviceIds) {
			InputDevice dev = InputDevice.getDevice(deviceId);
			int sources = dev.getSources();

			// Verify that the device has gamepad buttons, control sticks, or both.
			if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
			    || ((sources & InputDevice.SOURCE_JOYSTICK)	== InputDevice.SOURCE_JOYSTICK)) {
				return true;
			}
		}
		return false;
	}
}

/* [{
* (C) Copyright 2007 Nicolas Carranza and individual contributors.
* See the jpen-copyright.txt file in the jpen distribution for a full
* listing of individual contributors.
*
* This file is part of jpen.
*
* jpen is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* jpen is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with jpen.  If not, see <http://www.gnu.org/licenses/>.
* }] */
package jpen.provider.wintab;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.provider.AbstractPenProvider;
import jpen.provider.Utils;
import jpen.provider.VirtualScreenBounds;

public class WintabProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(WintabProvider.class.getName());
	public static final int PERIOD=10;
	final WintabAccess wintabAccess;
	private final Map<Integer, WintabDevice> cursorToDevice=new HashMap<Integer, WintabDevice>();
	private final PLevel.Range[] levelRanges=new PLevel.Range[PLevel.Type.values().length];
	final VirtualScreenBounds screenBounds=new VirtualScreenBounds();
	private final Thread thread;
	private boolean paused=true;
	private final Robot robot;

	public static class Constructor
		implements PenProvider.Constructor {
		private static Robot robot;
		@Override
		public String getName() {
			return "Wintab";
		}
		@Override
		public boolean constructable() {
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}

		@Override
		public PenProvider construct(PenManager pm) throws ConstructionException {
			try {
				Utils.loadLibrary();
				WintabAccess wintabAccess=new WintabAccess();
				if(robot==null)
					robot=new Robot();
				return new WintabProvider(pm, this, wintabAccess, robot);
			} catch(Throwable ex) {
				throw new ConstructionException(ex);
			}
		}
	}



	private WintabProvider(PenManager penManager, Constructor constructor, WintabAccess wintabAccess, Robot robot) {
		super(penManager, constructor);
		L.fine("start");
		this.wintabAccess=wintabAccess;
		this.robot=robot;

		for(PLevel.Type levelType: PLevel.Type.values())
			levelRanges[levelType.ordinal()]=wintabAccess.getLevelRange(levelType);

		thread=new Thread() {
						 public synchronized void run() {
							 try {
								 while(true) {
									 processQuedEvents();
									 wait(PERIOD);
									 while(paused){
										 L.fine("going to wait...");
										 wait();
										 L.fine("notified");
									 }
								 }
							 } catch(InterruptedException ex) { throw new Error(ex);}
						 }
					 }
					 ;
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

		// Defensive mechanism:
		penManager.component.addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent ev) {
						setPaused(false);
					}
					@Override
					public void mouseDragged(MouseEvent ev) {
						setPaused(false);
					}
				}
																							 );
		L.fine("end");
	}

	void moveMouseToLastScheduledLocation(Point2D.Float componentLocation) {
		robot.mouseMove(
			(int)(componentLocation.x+getPenManager().pen.lastScheduledState.getLevelValue(PLevel.Type.X)),
			(int)(componentLocation.y+getPenManager().pen.lastScheduledState.getLevelValue(PLevel.Type.Y))
		);
	}

	PLevel.Range getLevelRange(PLevel.Type type) {
		return levelRanges[type.ordinal()];
	}

	private void processQuedEvents() {
		L.fine("start");
		while(wintabAccess.nextPacket()) {
			WintabDevice device=getDevice(wintabAccess.getCursor());
			L.fine("device: ");
			L.fine(device.getName());
			device.scheduleEvents();
		}
		L.fine("end");
	}

	private WintabDevice getDevice(int cursor) {
		WintabDevice wintabDevice=cursorToDevice.get(cursor);
		if(wintabDevice==null) {
			cursorToDevice.put(cursor, wintabDevice=new WintabDevice(this, cursor));
			devices.clear();
			devices.addAll(cursorToDevice.values());
			getPenManager().firePenDeviceAdded(getConstructor(), wintabDevice);
		}
		return wintabDevice;
	}

	@Override
	public void penManagerPaused(boolean paused) {
		setPaused(paused);
	}

	void setPaused(boolean paused) {
		L.fine("start");
		if(paused==this.paused)
			return;
		this.paused=paused;
		wintabAccess.setEnabled(!paused);
		if(!paused){
			L.fine("false paused value");
			screenBounds.reset();
			synchronized(thread) {
				L.fine("going to notify all...");
				thread.notifyAll();
				L.fine("done notifying ");
			}
		}
		L.fine("end");
	}
}

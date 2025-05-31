/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Environment;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Position;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class HeroAction {
	
	public int dst;
	
	public static class Move extends HeroAction {
		public Move( int dst ) {
			super.recordPrevAction(dst, Environment.getTiles()[dst].terrain);
			this.dst = dst;
		}
	}
	
	public static class PickUp extends HeroAction {
		public PickUp( int dst ) {
			Object obj = Environment.getTiles()[dst].obj;

			if (obj.equals(Dungeon.hero)) {
				obj = Environment.getTiles()[dst].obj2;
			}

			super.recordPrevAction(dst, obj);
			this.dst = dst;
		}
	}
	
	public static class OpenChest extends HeroAction {
		public OpenChest( int dst ) {
			super.recordPrevAction(dst, Environment.getTiles()[dst].obj);
			this.dst = dst;
		}
	}
	
	public static class Buy extends HeroAction {
		public Buy( int dst ) {
			super.recordPrevAction(dst, Environment.getTiles()[dst].obj);
			this.dst = dst;
		}
	}
	
	public static class Interact extends HeroAction {
		public Char ch;
		public Interact( Char ch ) {
			super.recordPrevAction(ch.pos, ch);
			this.ch = ch;
		}
	}
	
	public static class Unlock extends HeroAction {
		public Unlock( int door ) {
			super.recordPrevAction(door, Environment.getTiles()[door].terrain);
			this.dst = door;
		}
	}
	
	public static class LvlTransition extends HeroAction {
		public LvlTransition(int stairs ) {
			super.recordPrevAction(stairs, Environment.getTiles()[stairs].terrain);
			this.dst = stairs;
		}
	}

	public static class Mine extends HeroAction {
		public Mine( int wall ) {
			super.recordPrevAction(wall, Environment.getTiles()[wall].terrain);
			this.dst = wall;
		}
	}
	
	public static class Alchemy extends HeroAction {
		public Alchemy( int pot ) {
			super.recordPrevAction(pot, Environment.getTiles()[pot].obj);
			this.dst = pot;
		}
	}
	
	public static class Attack extends HeroAction {
		public Char target;
		public Attack( Char target ) {
			super.recordPrevAction(target.pos, target);
			this.target = target;
		}
	}

	public void recordPrevAction(int dst, Object obj1) {
		if (!Dungeon.prevAction.isEmpty()) {
			return;
		}

		if (obj1 == null) {
			obj1 = "null";
		}

		Dungeon.prevAction.put( "action", this.toString() );
		Dungeon.prevAction.put( "obj1", obj1.toString() );
		Dungeon.prevAction.put( "tile", "(" + Position.transferPos2XY(dst)[0] + ", " + Position.transferPos2XY(dst)[1] + ")" );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

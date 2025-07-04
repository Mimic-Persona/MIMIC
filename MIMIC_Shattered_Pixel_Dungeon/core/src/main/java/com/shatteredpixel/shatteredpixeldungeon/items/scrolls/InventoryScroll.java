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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

public abstract class InventoryScroll extends Scroll {

	protected static boolean identifiedByUse = false;

	public boolean doIdentify( Item item ) {

		//FIXME this safety check shouldn't be necessary
		//it would be better to eliminate the curItem static variable.
		if (!(curItem instanceof InventoryScroll)){
			return false;
		}

		if (item != null) {
			Dungeon.prevAction.put( "action", "UseOnto" );
			Dungeon.prevAction.put( "obj1", this.trueName() );
			Dungeon.prevAction.put( "obj2", item.trueName() );

			if (!identifiedByUse) {
				curItem = detach(curUser.belongings.backpack);
			}
			((InventoryScroll)curItem).onItemSelected( item );
			((InventoryScroll)curItem).readAnimation();

			Sample.INSTANCE.play( Assets.Sounds.READ );

			return true;

		} else if (identifiedByUse && !((Scroll)curItem).anonymous) {

			((InventoryScroll)curItem).confirmCancelation();

			return true;

		} else if (((Scroll)curItem).anonymous) {
			Dungeon.prevAction.put( "action", "Read" );
			Dungeon.prevAction.put( "obj1", this.trueName() + " (ANONYMOUS)" );

			curUser.spendAndNext( TIME_TO_READ );

			return true;

		}
		return false;
	}

	@Override
	public void doRead() {
		
		if (!isKnown()) {
			identify();
			curItem = detach( curUser.belongings.backpack );
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}

		// If it is in the agent mode or manual mode, no need for item selector
		if (!Dungeon.isInAgentMode && !Dungeon.isInManualMode) {
			GameScene.selectItem( itemSelector );
		}
	}
	
	private void confirmCancelation() {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(this, "warning"),
				Messages.get(this, "yes"),
				Messages.get(this, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
				case 0:
					curUser.spendAndNext( TIME_TO_READ );
					identifiedByUse = false;
					break;
				case 1:
					GameScene.selectItem( itemSelector );
					break;
				}
			}
			public void onBackPressed() {}
		} );
	}

	private String inventoryTitle(){
		return Messages.get(this, "inv_title");
	}

	protected Class<?extends Bag> preferredBag = null;

	protected boolean usableOnItem( Item item ){
		return true;
	}
	
	protected abstract void onItemSelected( Item item );
	
	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return inventoryTitle();
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return preferredBag;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return usableOnItem(item);
		}

		@Override
		public void onSelect( Item item ) {
			
			//FIXME this safety check shouldn't be necessary
			//it would be better to eliminate the curItem static variable.
			if (!(curItem instanceof InventoryScroll)){
				return;
			}
			
			if (item != null) {

				if (!identifiedByUse) {
					curItem = detach(curUser.belongings.backpack);
				}
				((InventoryScroll)curItem).onItemSelected( item );
				((InventoryScroll)curItem).readAnimation();
				
				Sample.INSTANCE.play( Assets.Sounds.READ );
				
			} else if (identifiedByUse && !((Scroll)curItem).anonymous) {
				
				((InventoryScroll)curItem).confirmCancelation();
				
			} else if (((Scroll)curItem).anonymous) {

				curUser.spendAndNext( TIME_TO_READ );

			}
		}
	};
}

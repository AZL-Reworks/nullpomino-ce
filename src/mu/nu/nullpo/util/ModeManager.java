/*
    Copyright (c) 2010, Nullnoname, NullpoMino CE Contributors
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import mu.nu.nullpo.game.subsystem.mode.GameMode;

import org.apache.log4j.Logger;

/**
 * Mode 管理クラス
 */
public class ModeManager {
	/** Log */
	static Logger log = Logger.getLogger(ModeManager.class);

	/** Mode の動的配列 */
	public ArrayList<GameMode> modelist = new ArrayList<GameMode>();

	/**
	 * Constructor
	 */
	public ModeManager() {
	}

	/**
	 * Copy constructor
	 * @param m Copy source
	 */
	public ModeManager(ModeManager m) {
		modelist.addAll(m.modelist);
	}

	/**
	 * Mode のcountを取得(通常+ネットプレイ全部)
	 * @return Modeのcount(通常+ネットプレイ全部)
	 */
	public int getSize() {
		return modelist.size();
	}

	/**
	 * Mode のcountを取得
	 * @param netplay falseなら通常Mode だけ, When true,ネットプレイ用Mode だけcountえる
	 * @return Modeのcount
	 */
	public int getNumberOfModes(boolean netplay) {
		int count = 0;

		for(int i = 0; i < modelist.size(); i++) {
			GameMode mode = modelist.get(i);

			if((mode != null) && (mode.isNetplayMode() == netplay))
				count++;
		}

		return count;
	}

	/**
	 * 読み込まれている全てのMode nameを取得
	 * @return Mode nameの配列
	 */
	public String[] getAllModeNames() {
		String[] strings = new String[getSize()];

		for(int i = 0; i < strings.length; i++) {
			strings[i] = getName(i);
		}

		return strings;
	}

	/**
	 * 読み込まれているMode nameを取得
	 * @param netplay falseなら通常Mode だけ, When true,ネットプレイ用Mode だけ取得
	 * @return Mode nameの配列
	 */
	public String[] getModeNames(boolean netplay) {
		int num = getNumberOfModes(netplay);
		String[] strings = new String[num];
		int j = 0;

		for(int i = 0; i < modelist.size(); i++) {
			GameMode mode = modelist.get(i);

			if((mode != null) && (mode.isNetplayMode() == netplay)) {
				strings[j] = mode.getName();
				j++;
			}
		}

		return strings;
	}

	/**
	 * Mode  nameを取得
	 * @param id ModeID
	 * @return Mode name (idが不正なら「*INVALID MODE*」）
	 */
	public String getName(int id) {
		try {
			return modelist.get(id).getName();
		} catch(Exception e) {
			return "*INVALID MODE*";
		}
	}

	/**
	 * Mode  nameからIDを取得
	 * @param name Mode name
	 * @return ModeID (見つからない場合は-1）
	 */
	public int getIDbyName(String name) {
		if(name == null) return -1;

		for(int i = 0; i < modelist.size(); i++) {
			if(name.compareTo(modelist.get(i).getName()) == 0) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Mode オブジェクトを取得
	 * @param id ModeID
	 * @return Modeオブジェクト (idが不正ならnull）
	 */
	public GameMode getMode(int id) {
		try {
			return modelist.get(id);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Mode オブジェクトを取得
	 * @param name Mode name
	 * @return Modeオブジェクト (見つからないならnull）
	 */
	public GameMode getMode(String name) {
		try {
			return modelist.get(getIDbyName(name));
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Property fileに書かれた一覧からゲームMode を読み込み
	 * @param prop Property file
	 */
	public void loadGameModes(CustomProperties prop) {
		int count = 0;

		while(true) {
			// クラス名を読み込み
			String name = prop.getProperty(String.valueOf(count), null);
			if(name == null) return;

			Class<?> modeClass;
			GameMode modeObject;

			try {
				modeClass = Class.forName(name);
				modeObject = (GameMode) modeClass.newInstance();
				modelist.add(modeObject);
			} catch(ClassNotFoundException e) {
				log.warn("Mode class " + name + " not found", e);
			} catch(Exception e) {
				log.warn("Mode class " + name + " load failed", e);
			}

			count++;
		}
	}

	/**
	 * テキストファイルに書かれた一覧からゲームMode を読み込み
	 * @param bf テキストファイルを読み込んだBufferedReader
	 */
	public void loadGameModes(BufferedReader bf) {
		while(true) {
			// クラス名を読み込み
			String name = null;
			try {
				name = bf.readLine();
			} catch (IOException e) {
				log.warn("IOException on readLine()", e);
				return;
			}
			if(name == null) return;
			if(name.length() == 0) return;

			if(!name.startsWith("#")) {
				Class<?> modeClass;
				GameMode modeObject;

				try {
					modeClass = Class.forName(name);
					modeObject = (GameMode) modeClass.newInstance();
					modelist.add(modeObject);
				} catch(ClassNotFoundException e) {
					log.warn("Mode class " + name + " not found", e);
				} catch(Exception e) {
					log.warn("Mode class " + name + " load failed", e);
				}
			}
		}
	}
}

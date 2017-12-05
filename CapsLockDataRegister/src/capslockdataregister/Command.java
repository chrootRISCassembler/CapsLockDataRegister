/*  
    This file is part of CapsLockDataRegister. CapsLockDataRegister is a JSON generator for CapsLock.
    Copyright (C) 2017 RISCassembler

    CapsLockDataRegister is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package capslockdataregister;

/**
 * 開発者向けコンソールで実行できるコマンドの本体を内包するクラス.
 * <p>インスタンスを作らせないようにabstract修飾している.
 * このクラスを継承してはならない.</p>
 * 
 * <p>このクラス中の全ての関数はstaticにする.</p>
 */

abstract class Command{
    private Command(){
        System.err.println("CRITICAL : Instance of Command class is created.");
    }
    
    static final void dump(final String[] ParsedStringArray, MainFormController controller){
        controller.DisplayCollection.forEach(ele -> System.err.println(ele.getJSON()));
    }

    /**
     * 0-7の文字に対して"rwx"のようなパーミッションを表す三文字を返す.
     * <p>PosixFilePermissions.fromString()に渡す引数を構築するときに使う</p>
     * <p>0から7以外の文字を渡すと"---"を返す</p>
     * @param num 0から7の文字.本物のchmodに渡す700のような引数の一文字分.
     * @return "rwx"とか"r--"のような三文字.
     */
    static final String toPermString(char num){
        switch(num){
            case '1':
                return "--x";
            case '2':
                return "-w-";
            case '3':
                return "-wx";
            case '4':
                return "r--";
            case '5':
                return "r-x";
            case '6':
                return "rw-";
            case '7':
                return "rwx";
            default:
                return "---";
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * "でクォーティングされた文字列をパースする.
 * <p>このクラスはイミュータブル.
 * 一つでも異常なトークンがあるとhasErrorにtrueがセットされる.</p>
 * <p>"pathA", "Games/PathB", "Games/Image/PathC"のような文字列を pathA Games/PathB Games/Images/PathCのように
 * "を取り除き個々の文字列をトークンとして保存,get()で文字列のリストとして返す.</p>
 */
final class QuotedStringParser {
    private enum State{
        WatingBeginQuote,
        WatingTokenFirst,
        Escaped,
        WatingEndQuote
    }
    
    private boolean hasError = false;
    private final List<String> Tokens = new ArrayList<>();

    /**
     * パスが複数含まれる文字列をパースする.
     * <p>全てのパスは"でクォーティングされていなければならない.
     * @param RawString 正常に記述されているか分からないパース対象文字列.
     */
    public QuotedStringParser(String RawString) {
        State ParseState = State.WatingBeginQuote;
        
        int TokenFirst = 0;
        
        for(int i = 0;i != RawString.length();++i){
            final char ch = RawString.charAt(i);
            switch(ch){
                case '\"':
                    switch(ParseState){
                        case WatingBeginQuote:
                            ParseState = State.WatingTokenFirst;
                            break;
                        case WatingTokenFirst:// ""がある
                            hasError = true;
                            ParseState = State.WatingBeginQuote;
                            break;
                        case Escaped:// \"がある
                            ParseState = State.WatingEndQuote;
                            break;
                        case WatingEndQuote:
                            String Token = RawString.substring(TokenFirst, i);
                            Tokens.add(Token.replace("\\\"", "\"").replace("\\\\", "\\"));
                            ParseState = State.WatingBeginQuote;
                            break;
                    }
                break;
                
                case '\\':
                    switch(ParseState){
                        case WatingTokenFirst:
                            TokenFirst = i;
                        case WatingEndQuote://フォールスルー
                            ParseState = State.Escaped;
                            break;
                        case Escaped:
                            ParseState = State.WatingEndQuote;
                            break;
                    }
                break;
                
                default:
                    switch(ParseState){
                        case WatingTokenFirst:
                            TokenFirst = i;
                        case Escaped://フォールスルー
                            ParseState = State.WatingEndQuote;
                            break;
                    }
            }
        }
    }

    /**
     * 解析された文字列のリストを返す.
     */
    final List<String> get(){return Tokens;};
    
    /**
     * 解析中にエラーが発生したかどうか.
     */
    final boolean hasError(){return this.hasError;}
}

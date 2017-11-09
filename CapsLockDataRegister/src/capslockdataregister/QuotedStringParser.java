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
                    if(ParseState == State.WatingTokenFirst){
                        TokenFirst = i;
                        ParseState = State.WatingEndQuote;
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

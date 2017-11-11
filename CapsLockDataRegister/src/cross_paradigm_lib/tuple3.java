package cross_paradigm_lib;

/**
 * タプル.3組み.
 */

public class tuple3 <A, B, C> implements Ituple{
    A a;
    B b;
    C c;
    public tuple3(A a, B b, C c){
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    @Override
    public final int size(){return 3;};
    
    @Override
    public final A getA(){return a;}
    
    @Override
    public final B getB(){return b;}
    
    @Override
    public final C getC(){return c;}
}

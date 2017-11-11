package cross_paradigm_lib;

/**
 * タプル.4組み.
 */

public class tuple4 <A, B, C, D> implements Ituple{
    A a;
    B b;
    C c;
    D d;
    public tuple4(A a, B b, C c, D d){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    
    @Override
    public final int size(){return 4;};
    
    @Override
    public final A getA(){return a;}
    
    @Override
    public final B getB(){return b;}
    
    @Override
    public final C getC(){return c;}
    
    @Override
    public final D getD(){return d;}
}

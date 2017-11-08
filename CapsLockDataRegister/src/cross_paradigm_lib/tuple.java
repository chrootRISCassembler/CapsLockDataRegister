package cross_paradigm_lib;

/**
 * タプル.2組み.
 */

public final class tuple<A, B> implements Ituple{
    A a;
    B b;
    tuple(A a, B b){
        this.a = a;
        this.b = b;
    }
    
    @Override
    public final int size(){return 2;};
    
    @Override
    public final A getA(){return a;}
    
    @Override
    public final B getB(){return b;}
}

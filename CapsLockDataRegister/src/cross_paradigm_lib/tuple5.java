package cross_paradigm_lib;

/**
 * タプル.5組み.
 */

public class tuple5 <A, B, C, D, E> implements Ituple{
    A a;
    B b;
    C c;
    D d;
    E e;
    public tuple5(A a, B b, C c, D d, E e){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }
    
    @Override
    public final int size(){return 5;};
    
    @Override
    public final A getA(){return a;}
    
    @Override
    public final B getB(){return b;}
    
    @Override
    public final C getC(){return c;}
    
    @Override
    public final D getD(){return d;}
    
    @Override
    public final E getE(){return e;}
}

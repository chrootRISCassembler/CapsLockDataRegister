package cross_paradigm_lib;

/**
 * tupleのインターフェイス.
 * <p>tupleの実装はイミュータブル.存在しない要素にget()しようとするとnullを返す.</p>
 * 
 */

public interface Ituple <A, B, C, D, E> {
    /**
     * tupleのサイスを返す.
     * 例えばtuplenなら2.tuple5なら5.
     */
    default public int size(){return 0;};
    
    /**
     * 1つ目の要素を返す.
     */
    default public A getA(){return null;};
    /**
     * 2つ目の要素を返す.
     */
    default public B getB(){return null;};
    /**
     * 3つ目の要素を返す.ない場合nullを返す.
     */
    default public C getC(){return null;};
    /**
     * 4つ目の要素を返す.ない場合nullを返す.
     */
    default public D getD(){return null;};
    /**
     * 5つ目の要素を返す.ない場合nullを返す.
     */
    default public E getE(){return null;};
    
    static public <A, B> tuple<A, B> of(A a, B b){
        return new tuple<>(a, b);
    }
    
    static public <A, B, C> tuple3<A, B, C> of(A a, B b, C c){
        return new tuple3<>(a, b, c);
    }
    
    static public <A, B, C, D> tuple4<A, B, C, D> of(A a, B b, C c, D d){
        return new tuple4<>(a, b, c, d);
    }
    
    static public <A, B, C, D, E> tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e){
        return new tuple5<>(a, b, c, d, e);
    }
}

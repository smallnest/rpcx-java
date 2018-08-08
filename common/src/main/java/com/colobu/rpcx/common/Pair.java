package com.colobu.rpcx.common;

import java.util.Objects;

/**
 * @author goodjava@qq.com
 */
public class Pair<T1, T2> {
    private T1 object1;
    private T2 object2;


    public Pair(T1 object1, T2 object2) {
        this.object1 = object1;
        this.object2 = object2;
    }


    public static <T1, T2> Pair<T1, T2> of(T1 object, T2 object2) {
        return new Pair<>(object, object2);
    }


    public T1 getObject1() {
        return object1;
    }


    public void setObject1(T1 object1) {
        this.object1 = object1;
    }


    public T2 getObject2() {
        return object2;
    }


    public void setObject2(T2 object2) {
        this.object2 = object2;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getObject1(), pair.getObject1()) &&
                Objects.equals(getObject2(), pair.getObject2());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObject1(), getObject2());
    }
}

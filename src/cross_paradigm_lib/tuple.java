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

package cross_paradigm_lib;

/**
 * タプル.2組み.
 */

public final class tuple<A, B> implements Ituple{
    A a;
    B b;
    public tuple(A a, B b){
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

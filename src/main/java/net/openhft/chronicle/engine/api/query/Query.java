/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.openhft.chronicle.engine.api.query;

import net.openhft.chronicle.core.util.SerializableFunction;
import net.openhft.chronicle.core.util.SerializablePredicate;

import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by peter.lawrey on 11/07/2015.
 */
public interface Query<T> {

    Query<T> filter(SerializablePredicate<? super T> predicate);

    <R> Query<R> map(SerializableFunction<? super T, ? extends R> mapper);

    <R> Query<R> project(Class<R> rClass);

    <R> Query<R> flatMap(SerializableFunction<? super T, ? extends Query<? extends R>> mapper);

    Stream<T> stream();

    Subscription subscribe(Consumer<? super T> action);

    <R, A> R collect(Collector<? super T, A, R> collector);

    void forEach(Consumer<? super T> action);

}

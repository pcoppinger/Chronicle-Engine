/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.engine.cfg;

import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.ObjectKeyValueStore;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.wire.WireIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ChronicleMapCfg implements Installable {
    private Class keyType, valueType;
    private boolean putReturnsNull, removeReturnsNull;
    private String compression;
    private String diskPath;
    private long entries = -1;
    private double averageKeySize = -1;
    private double averageValueSize = -1;

    @Nullable
    @Override
    public Void install(@NotNull String path, @NotNull AssetTree assetTree) {
         @NotNull Asset asset = assetTree.acquireAsset(path);
        ((VanillaAsset) asset).enableTranslatingValuesToBytesStore();
        @NotNull RequestContext rc = RequestContext.requestContext(path);
        rc.basePath(diskPath)
                .putReturnsNull(putReturnsNull)
                .removeReturnsNull(removeReturnsNull)
                .keyType(keyType)
                .valueType(valueType);

        if (entries != -1) rc.entries(entries);
        if (averageKeySize != -1) rc.averageKeySize(averageKeySize);
        if (averageValueSize != -1) rc.averageValueSize(averageValueSize);

        @NotNull ChronicleMapKeyValueStore chronicleMapKeyValueStore = new ChronicleMapKeyValueStore(rc, asset);
        asset.addView(ObjectKeyValueStore.class, chronicleMapKeyValueStore);

        return null;
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IllegalStateException {
        wire.read(() -> "diskPath").text(this, (o, c) -> o.diskPath = c)
                .read(() -> "keyType").typeLiteral(this, (o, c) -> o.keyType = c)
                .read(() -> "valueType").typeLiteral(this, (o, c) -> o.valueType = c)
                .read(() -> "compression").text(this, (o, c) -> o.compression = c)
                .read(() -> "putReturnsNull").bool(this, (o, e) -> o.putReturnsNull = e)
                .read(() -> "removeReturnsNull").bool(this, (o, e) -> o.removeReturnsNull = e)
                .read(() -> "entries").int64(this, (o, e) -> o.entries = e)
                .read(() -> "averageKeySize").float64(this, (o, e) -> o.averageKeySize = e)
                .read(() -> "averageValueSize").float64(this, (o, e) -> o.averageValueSize = e);
    }

    @NotNull
    @Override
    public String toString() {
        return "ChronicleMapCfg{" +
                "keyType=" + keyType +
                ", valueType=" + valueType +
                ", putReturnsNull=" + putReturnsNull +
                ", removeReturnsNull=" + removeReturnsNull +
                ", compression='" + compression + '\'' +
                '}';
    }
}

/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.map.MapWireHandler;
import net.openhft.chronicle.network2.WireHandler;
import net.openhft.chronicle.network2.WireTcpHandler;
import net.openhft.chronicle.wire.BinaryWire;
import net.openhft.chronicle.wire.RawWire;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;

import static net.openhft.chronicle.map.MapWireHandler.Fields.RESULT;
import static net.openhft.chronicle.map.MapWireHandler.Fields.TRANSACTION_ID;

/**
 * Created by Rob Austin
 */
public class EngineWireHandler extends WireTcpHandler {


    public static final String TEXT_WIRE = TextWire.class.getSimpleName();
    public static final String BINARY_WIRE = BinaryWire.class.getSimpleName();
    public static final String RAW_WIRE = RawWire.class.getSimpleName();

    private final CharSequence preferredWireType = new StringBuilder(TextWire.class.getSimpleName());
    private final StringBuilder text = new StringBuilder();

    @NotNull
    private final WireHandler mapWireHandler;

    @NotNull
    private final WireHandler queueWireHandler;

    @NotNull
    private final WireHandler coreWireHandler = new CoreWireHandler();

    public EngineWireHandler(@NotNull final WireHandler mapWireHandler,
                             final WireHandler queueWireHandler) {
        this.mapWireHandler = mapWireHandler;
        this.queueWireHandler = queueWireHandler;
    }

    @Override
    protected void process(Wire in, Wire out) throws StreamCorruptedException {

        in.read(MapWireHandler.Fields.TYPE).text(text);

        if ("MAP".contentEquals(text)) {
            mapWireHandler.process(in, out);
            return;
        }

        if ("QUEUE".contentEquals(text)) {
            queueWireHandler.process(in, out);
            return;
        }

        if ("CORE".contentEquals(text))
            coreWireHandler.process(in, out);
    }

    protected Wire createWriteFor(Bytes bytes) {

        if (TEXT_WIRE.contentEquals(preferredWireType))
            return new TextWire(bytes);

        if (BINARY_WIRE.contentEquals(preferredWireType))
            return new BinaryWire(bytes);

        if (RAW_WIRE.contentEquals(preferredWireType))
            return new RawWire(bytes);

        throw new IllegalStateException("preferredWireType=" + preferredWireType + " is not supported.");

    }

    class CoreWireHandler implements WireHandler {

        public void process(Wire in, Wire out) {

            long transactionId = inWire.read(TRANSACTION_ID).int64();
            outWire.write(TRANSACTION_ID).int64(transactionId);

            in.read(MapWireHandler.Fields.METHOD_NAME).text(text);

            if ("getWireFormats".contentEquals(text)) {
                out.write(RESULT).text(TEXT_WIRE + "," + BINARY_WIRE);
                return;
            }

            if ("setWireFormat".contentEquals(text)) {
                out.write(RESULT).text(preferredWireType);
                recreateWire(true);
            }
        }

    }

}
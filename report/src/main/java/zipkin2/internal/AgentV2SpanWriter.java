/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zipkin2.internal;

import com.google.common.collect.ImmutableList;
import com.megaease.easeagent.report.trace.TraceProps;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.Span;

import java.util.Collection;
import java.util.function.Supplier;

public class AgentV2SpanWriter implements WriteBuffer.Writer<Span> {

    public final Collection<WriteBuffer.Writer<Span>> writerList;

    @Deprecated
    public AgentV2SpanWriter() {
        this(()->"",  null);
    }

    public AgentV2SpanWriter(Supplier<String> service, TraceProps properties) {
        writerList = ImmutableList.<WriteBuffer.Writer<Span>>builder()
                .add(new AgentV2SpanBaseWriter())
                .add(new AgentV2SpanLocalEndpointWriter())
                .add(new AgentV2SpanRemoteEndpointWriter())
                .add(new AgentV2SpanAnnotationsWriter())
                .add(new AgentV2SpanTagsWriter())
                .add(new AgentV2SpanGlobalWriter("log-tracing", service, properties))
                .build();
    }


    public int sizeInBytes(Span value) {
        final MutableInt size = new MutableInt(1); // 1 byte for first {
        writerList.forEach(w -> {
            size.add(w.sizeInBytes(value));
        });
        size.add(1); // 1 byte for last }
        return size.intValue();
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        buffer.writeByte(123); //write '{'
        writerList.forEach(w -> {
            w.write(value, buffer);
        });
        buffer.writeByte(125); // write last '}'
    }

    public String toString() {
        return "Span";
    }
}

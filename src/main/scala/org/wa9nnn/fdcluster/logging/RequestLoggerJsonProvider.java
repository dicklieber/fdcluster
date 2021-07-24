package org.wa9nnn.fdcluster.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;

public class RequestLoggerJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent>
{
    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event ) throws IOException {
        try {



            String message = event.getFormattedMessage();

            JsonWritingUtils.writeStringField( generator, "message", message );



//
//            String[] split = message.split( "\t" );
//            String remoteAddress = split[0];
//            String method = split[1];
//            String url = split[2];
//            String servletPath = split[3];
//            String requestMapping = split[4];
//            String handlerName = split[5];
//            String viewName = split[6];
//            int status = Integer.parseInt( split[7] );
//            int duration = Integer.parseInt( split[8] );
//            JsonWritingUtils.writeStringField( generator, "remoteAddress", remoteAddress );
//            JsonWritingUtils.writeStringField( generator, "method", method );
//            JsonWritingUtils.writeStringField( generator, "url", url );
//            JsonWritingUtils.writeStringField( generator, "servletPath", servletPath );
//            JsonWritingUtils.writeStringField( generator, "requestMapping", requestMapping );
//            JsonWritingUtils.writeStringField( generator, "handlerName", handlerName );
//            JsonWritingUtils.writeStringField( generator, "viewName", viewName );
//            JsonWritingUtils.writeNumberField( generator, "status", status );
//            JsonWritingUtils.writeNumberField( generator, "duration", duration );
        }
        catch ( Exception e ) {
            throw new IOException( e );
        }
    }
}
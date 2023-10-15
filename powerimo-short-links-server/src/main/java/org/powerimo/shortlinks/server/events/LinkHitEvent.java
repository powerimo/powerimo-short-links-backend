package org.powerimo.shortlinks.server.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class LinkHitEvent extends ApplicationEvent {
    private final String code;
    private final String agentString;
    private final String remoteHost;

    public LinkHitEvent(Object source, String code, String agentString, String remoteHost) {
        super(source);
        this.code = code;
        this.agentString = agentString;
        this.remoteHost = remoteHost;
    }
}

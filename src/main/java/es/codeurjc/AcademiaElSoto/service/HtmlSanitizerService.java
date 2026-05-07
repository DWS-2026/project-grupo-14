package es.codeurjc.AcademiaElSoto.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    private final PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements(
                    "p", "br",
                    "strong", "b",
                    "em", "i",
                    "u", "s",
                    "blockquote",
                    "pre", "code",
                    "ul", "ol", "li",
                    "h1", "h2", "h3",
                    "span",
                    "a"
            )
            
            .allowUrlProtocols("http", "https", "mailto")
            .allowAttributes("href").onElements("a")
            .allowAttributes("class")
            .matching(
                    true,
                    "ql-align-center",
                    "ql-align-right",
                    "ql-align-justify",
                    "ql-size-small",
                    "ql-size-large",
                    "ql-size-huge",
                    "ql-indent-1",
                    "ql-indent-2",
                    "ql-indent-3",
                    "ql-indent-4"
            )
            .onElements("p", "span", "li")
            .toFactory();

    public String sanitize(String html) {
        if (html == null) {
            return "";
        }

        return policy.sanitize(html.trim());
    }
}
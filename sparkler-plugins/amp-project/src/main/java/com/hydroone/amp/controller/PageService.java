/*s
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydroone.amp.controller;

import com.hydroone.amp.page.PagesRepository;
import com.hydroone.amp.page.model.Page;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author michelad
 */
@Service
public class PageService {

    final PagesRepository pagesRepository;
    public static final int DEFAULT_PAGE_SIZE = 1000;
    public static final String HIEGHT = "height";
    public static final String WIDTH = "width";

    public static final String TAG = "tag";
    public static final String SELECT = "select";
    public static final String CLASS = "class";
    public static final String ID = "id";
    public static final String ATTRIBUTE = "attribute";

    public static final String IMG = "img";
    public static final String SRC = "src";
    public static final String STYLE = "style";

    String hostname;

    @Autowired
    public PageService(PagesRepository pagesRepository, @Value("${h1.default.fullhostname}") String hostname) {
        this.pagesRepository = pagesRepository;
        this.hostname = hostname;
    }

    public org.springframework.data.domain.Page<Page> find(Pageable pageable) {
        return pagesRepository.findAll(pageable);
    }

    public Page findByUrlEndingWith(String urlFragment, Pageable pageable) {

        org.springframework.data.domain.Page pages = pagesRepository.findByUrlEndingWith(urlFragment, pageable);

        if (pages.getTotalElements() == 1) {

            Page page = (Page) pages.getContent().get(0);

            page.setBody(resolveURL(page.getBody(), hostname));
            page.setBody(makeAMPTags(page.getBody()));

            return page;
        }
        return null;
    }

    private String resolveURL(String body, String resolveUrl) {
        Document document = Jsoup.parse(body);
//        Elements links = document.getElementsByTag("img");
//        String attr;

        getElementToResolveUrl(document, hostname, TAG, IMG);
        getElementToResolveUrl(document, hostname, ID, "bannerImage");

//        for (Element link : links) {
//            attr = "src";
//            String src = link.attr(attr);
//            if (src.startsWith("/")) {
//                link.attr(attr, resolveUrl + src);
//            }
//        }
//   Elements links = document.getElementsByTag(img);
        return document.outerHtml();
    }

    private Document getElementToResolveUrl(Document document, String url, String queryType, String query) {
        switch (queryType) {
            case TAG:
                Elements elements = document.getElementsByTag(query);
                addUrlToElements(elements, url, SRC);
                addUrlToElements(elements, url, STYLE);
                break;
            case ID:
                Element element = document.getElementById(query);
                addUrlToElement(element, url, STYLE);
                break;
        }
        return document;
    }

    private Elements addUrlToElements(Elements elements, String hostname, String attr) {
        for (Element element : elements) {
            addUrlToElement(element, hostname, attr);
        }
        return elements;
    }

    private Element addUrlToElement(Element element, String hostname, String attr) {
        String value = element.attr(attr);
        switch (attr) {
            case STYLE:
                if (value.contains("url")) {
                    String regex = "\\/\\w.+\\/?";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(value);
                    String regResVal = m.group();
                    element.attr(attr, hostname + regResVal);
                }
                break;
            default:
                if (value.startsWith("/"))
                    element.attr(attr, hostname + value);
                break;
        }
        return element;
    }

    private String makeAMPTags(String body) {
        Document document = Jsoup.parse(body);
        document = convertTagToAmpTag(document, "img", "amp-img");
        return document.outerHtml();
    }

    private Document convertTagToAmpTag(Document document, String tag, String ampTag) {

        Elements elements = document.getElementsByTag(tag);
        if (tag.equals("img")) {
            elements.attr("width", "500");
            elements.attr("height", "300");
//            elements.attr("layout", "intrinsic");
        }

        elements.tagName(ampTag);
        return document;
    }
}
